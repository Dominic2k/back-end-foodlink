package org.datpham.foodlink.service.impl;

import lombok.RequiredArgsConstructor;
import org.datpham.foodlink.dto.request.OrderRequest;
import org.datpham.foodlink.dto.response.OrderResponse;
import org.datpham.foodlink.entity.Ingredient;
import org.datpham.foodlink.entity.Order;
import org.datpham.foodlink.entity.OrderItem;
import org.datpham.foodlink.entity.OrderItemIngredient;
import org.datpham.foodlink.entity.Recipe;
import org.datpham.foodlink.entity.RecipeIngredient;
import org.datpham.foodlink.entity.User;
import org.datpham.foodlink.exception.BusinessException;
import org.datpham.foodlink.repository.IngredientRepository;
import org.datpham.foodlink.repository.OrderRepository;
import org.datpham.foodlink.repository.RecipeRepository;
import org.datpham.foodlink.repository.UserRepository;
import org.datpham.foodlink.service.OrderService;
import org.datpham.foodlink.util.IngredientUnitSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(String status, Pageable pageable) {
        Page<Order> orders;

        if (status != null && !status.isBlank()) {
            try {
                Order.OrderStatus statusEnum = Order.OrderStatus.valueOf(status);
                orders = orderRepository.findByStatus(statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                orders = orderRepository.findAll(pageable);
            }
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(this::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Page<Order> orders = orderRepository.findByUserEmailOrderByCreatedAtDesc(email, pageable);
        return orders.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrderById(String id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Order order = orderRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new BusinessException("Order not found", HttpStatus.NOT_FOUND));
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelMyOrder(String id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Order order = orderRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new BusinessException("Order not found", HttpStatus.NOT_FOUND));

        Order.OrderStatus currentStatus = order.getStatus();
        if (currentStatus != Order.OrderStatus.pending) {
            throw new BusinessException("Only pending orders can be canceled", HttpStatus.BAD_REQUEST);
        }

        adjustStockFromExistingOrder(order, false);
        order.setStatus(Order.OrderStatus.canceled);
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Order not found", HttpStatus.NOT_FOUND));
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Order not found", HttpStatus.NOT_FOUND));

        Order.OrderStatus newStatus;
        try {
            newStatus = Order.OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: " + status, HttpStatus.BAD_REQUEST);
        }

        Order.OrderStatus oldStatus = order.getStatus();
        if (oldStatus == newStatus) {
            return toResponse(order);
        }

        if (oldStatus == Order.OrderStatus.canceled && newStatus != Order.OrderStatus.canceled) {
            adjustStockFromExistingOrder(order, true);
        } else if (oldStatus != Order.OrderStatus.canceled && newStatus == Order.OrderStatus.canceled) {
            adjustStockFromExistingOrder(order, false);
        }

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.UNAUTHORIZED));

        PreparedOrder preparedOrder = prepareOrder(request.getItems());

        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.pending);
        order.setDeliveryAddressText(request.getDeliveryAddressText());
        order.setDeliveryPhone(request.getDeliveryPhone());
        order.setNote(request.getNote());
        order.setTotalAmount(preparedOrder.totalAmount());
        order.setPaymentMethod(request.getPaymentMethod());

        for (PreparedOrderItem preparedItem : preparedOrder.items()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setRecipe(preparedItem.recipe());
            item.setServings(preparedItem.servings());
            item.setPricePerServingSnapshot(preparedItem.pricePerServingSnapshot());
            item.setLineTotal(preparedItem.lineTotal());

            for (PreparedIngredientSnapshot preparedSnapshot : preparedItem.ingredientSnapshots()) {
                OrderItemIngredient snapshot = new OrderItemIngredient();
                snapshot.setOrderItem(item);
                snapshot.setIngredient(preparedSnapshot.ingredient());
                snapshot.setIngredientNameSnapshot(preparedSnapshot.ingredient().getName());
                snapshot.setQuantityBase(scaleQuantity(preparedSnapshot.quantityBase()));
                snapshot.setBaseUnit(preparedSnapshot.ingredient().getBaseUnit());
                snapshot.setUnitPriceSnapshot(preparedSnapshot.unitPriceSnapshot());
                snapshot.setLineTotal(preparedSnapshot.lineTotal());
                item.getIngredientSnapshots().add(snapshot);
            }

            order.getOrderItems().add(item);
        }

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    private PreparedOrder prepareOrder(List<OrderRequest.OrderItemRequest> requests) {
        List<PendingOrderItem> pendingItems = new ArrayList<>();
        List<String> ingredientIds = new ArrayList<>();

        for (OrderRequest.OrderItemRequest requestItem : requests) {
            Recipe recipe = recipeRepository.findById(requestItem.getRecipeId())
                    .orElseThrow(() -> new BusinessException("Recipe not found: " + requestItem.getRecipeId(), HttpStatus.BAD_REQUEST));

            if (recipe.getRecipeIngredients() == null || recipe.getRecipeIngredients().isEmpty()) {
                throw new BusinessException("Recipe has no ingredients: " + recipe.getName(), HttpStatus.BAD_REQUEST);
            }

            int servings = requestItem.getServings() == null || requestItem.getServings() <= 0 ? 1 : requestItem.getServings();
            int baseServings = recipe.getBaseServings() == null || recipe.getBaseServings() <= 0 ? 1 : recipe.getBaseServings();
            BigDecimal multiplier = BigDecimal.valueOf(servings)
                    .divide(BigDecimal.valueOf(baseServings), 6, RoundingMode.HALF_UP);

            List<PendingIngredientSelection> ingredientSelections = new ArrayList<>();
            if (requestItem.getCustomIngredients() != null && !requestItem.getCustomIngredients().isEmpty()) {
                for (OrderRequest.CustomIngredientRequest customIng : requestItem.getCustomIngredients()) {
                    BigDecimal customQty = BigDecimal.valueOf(customIng.getQuantity());
                    ingredientSelections.add(new PendingIngredientSelection(
                            customIng.getIngredientId(),
                            customQty,
                            customIng.getUnit()
                    ));
                    ingredientIds.add(customIng.getIngredientId());
                }
            } else {
                for (RecipeIngredient recipeIngredient : recipe.getRecipeIngredients()) {
                    BigDecimal scaledQuantity = recipeIngredient.getQuantity() == null
                            ? ZERO
                            : recipeIngredient.getQuantity().multiply(multiplier);
                    ingredientSelections.add(new PendingIngredientSelection(
                            recipeIngredient.getIngredientId(),
                            scaledQuantity,
                            recipeIngredient.getUnit()
                    ));
                    ingredientIds.add(recipeIngredient.getIngredientId());
                }
            }

            pendingItems.add(new PendingOrderItem(recipe, servings, ingredientSelections));
        }

        Map<String, Ingredient> ingredientMap = ingredientRepository.findAllByIdInForUpdate(ingredientIds).stream()
                .collect(Collectors.toMap(Ingredient::getId, ingredient -> ingredient, (left, right) -> left, LinkedHashMap::new));

        Map<String, BigDecimal> requiredByIngredientId = new HashMap<>();
        List<PreparedOrderItem> preparedItems = new ArrayList<>();

        for (PendingOrderItem pendingItem : pendingItems) {
            List<PreparedIngredientSnapshot> preparedSnapshots = new ArrayList<>();
            BigDecimal itemTotal = ZERO;

            for (PendingIngredientSelection selection : pendingItem.ingredientSelections()) {
                Ingredient ingredient = ingredientMap.get(selection.ingredientId());
                if (ingredient == null) {
                    throw new BusinessException("Ingredient not found: " + selection.ingredientId(), HttpStatus.BAD_REQUEST);
                }
                validatePurchasableIngredient(ingredient);

                BigDecimal quantityBase = IngredientUnitSupport.convertToBaseQuantity(
                        ingredient,
                        selection.quantity(),
                        selection.unit()
                );
                if (quantityBase == null) {
                    throw new BusinessException(
                            "Cannot convert recipe ingredient unit '" + selection.unit() + "' to base unit '" + ingredient.getBaseUnit()
                                    + "' for ingredient " + ingredient.getName(),
                            HttpStatus.BAD_REQUEST
                    );
                }

                BigDecimal lineTotal = IngredientUnitSupport.calculateLinePriceFromBaseQuantity(ingredient, quantityBase);
                if (lineTotal == null) {
                    throw new BusinessException("Ingredient price is missing: " + ingredient.getName(), HttpStatus.BAD_REQUEST);
                }

                requiredByIngredientId.merge(
                        ingredient.getId(),
                        quantityBase,
                        BigDecimal::add
                );

                BigDecimal money = scaleMoney(lineTotal);
                preparedSnapshots.add(new PreparedIngredientSnapshot(
                        ingredient,
                        quantityBase,
                        ingredient.getPricePerBaseUnit(),
                        money
                ));
                itemTotal = itemTotal.add(money);
            }

            BigDecimal lineTotal = scaleMoney(itemTotal);
            BigDecimal pricePerServingSnapshot = lineTotal.divide(
                    BigDecimal.valueOf(pendingItem.servings()),
                    2,
                    RoundingMode.HALF_UP
            );
            preparedItems.add(new PreparedOrderItem(
                    pendingItem.recipe(),
                    pendingItem.servings(),
                    pricePerServingSnapshot,
                    lineTotal,
                    preparedSnapshots
            ));
        }

        validateAndDeductStock(ingredientMap, requiredByIngredientId);

        BigDecimal totalAmount = preparedItems.stream()
                .map(PreparedOrderItem::lineTotal)
                .reduce(ZERO, BigDecimal::add);

        return new PreparedOrder(preparedItems, scaleMoney(totalAmount));
    }

    private void validatePurchasableIngredient(Ingredient ingredient) {
        if (!Boolean.TRUE.equals(ingredient.getIsActive())) {
            throw new BusinessException("Ingredient is inactive: " + ingredient.getName(), HttpStatus.BAD_REQUEST);
        }
        if (ingredient.getBaseUnit() == null || ingredient.getBaseUnit().isBlank()) {
            throw new BusinessException("Ingredient base unit is missing: " + ingredient.getName(), HttpStatus.BAD_REQUEST);
        }
        if (ingredient.getPricePerBaseUnit() == null) {
            throw new BusinessException("Ingredient price is missing: " + ingredient.getName(), HttpStatus.BAD_REQUEST);
        }
        if (ingredient.getStockQuantityBase() == null) {
            ingredient.setStockQuantityBase(ZERO);
        }
    }

    private void validateAndDeductStock(Map<String, Ingredient> ingredientMap, Map<String, BigDecimal> requiredByIngredientId) {
        for (Map.Entry<String, BigDecimal> entry : requiredByIngredientId.entrySet()) {
            Ingredient ingredient = ingredientMap.get(entry.getKey());
            BigDecimal required = scaleQuantity(entry.getValue());
            BigDecimal currentStock = zeroIfNull(ingredient.getStockQuantityBase());

            if (currentStock.compareTo(required) < 0) {
                throw new BusinessException(
                        "Insufficient stock for ingredient " + ingredient.getName()
                                + ": required=" + required.stripTrailingZeros().toPlainString()
                                + " " + ingredient.getBaseUnit()
                                + ", available=" + currentStock.stripTrailingZeros().toPlainString()
                                + " " + ingredient.getBaseUnit(),
                        HttpStatus.BAD_REQUEST
                );
            }

            ingredient.setStockQuantityBase(scaleQuantity(currentStock.subtract(required)));
        }
    }

    private void adjustStockFromExistingOrder(Order order, boolean deduct) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return;
        }

        Map<String, BigDecimal> quantityByIngredientId = new HashMap<>();
        for (OrderItem item : order.getOrderItems()) {
            if (item.getIngredientSnapshots() == null) {
                continue;
            }
            for (OrderItemIngredient snapshot : item.getIngredientSnapshots()) {
                if (snapshot.getIngredient() == null || snapshot.getIngredient().getId() == null) {
                    continue;
                }
                quantityByIngredientId.merge(
                        snapshot.getIngredient().getId(),
                        zeroIfNull(snapshot.getQuantityBase()),
                        BigDecimal::add
                );
            }
        }

        if (quantityByIngredientId.isEmpty()) {
            return;
        }

        Map<String, Ingredient> ingredientMap = ingredientRepository.findAllByIdInForUpdate(quantityByIngredientId.keySet()).stream()
                .collect(Collectors.toMap(Ingredient::getId, ingredient -> ingredient));

        for (Map.Entry<String, BigDecimal> entry : quantityByIngredientId.entrySet()) {
            Ingredient ingredient = ingredientMap.get(entry.getKey());
            if (ingredient == null) {
                continue;
            }

            BigDecimal quantity = scaleQuantity(entry.getValue());
            BigDecimal currentStock = zeroIfNull(ingredient.getStockQuantityBase());

            if (deduct) {
                if (currentStock.compareTo(quantity) < 0) {
                    throw new BusinessException(
                            "Insufficient stock to reopen order for ingredient " + ingredient.getName(),
                            HttpStatus.BAD_REQUEST
                    );
                }
                ingredient.setStockQuantityBase(scaleQuantity(currentStock.subtract(quantity)));
            } else {
                ingredient.setStockQuantityBase(scaleQuantity(currentStock.add(quantity)));
            }
        }
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getOrderItems() != null
                ? order.getOrderItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .recipeId(item.getRecipe() != null ? item.getRecipe().getId() : null)
                        .recipeName(item.getRecipe() != null ? item.getRecipe().getName() : "Unknown")
                        .servings(item.getServings())
                        .pricePerServingSnapshot(item.getPricePerServingSnapshot())
                        .lineTotal(item.getLineTotal())
                        .ingredients(item.getIngredientSnapshots() != null
                                ? item.getIngredientSnapshots().stream()
                                .map(snapshot -> OrderResponse.OrderIngredientResponse.builder()
                                        .ingredientId(snapshot.getIngredient() != null ? snapshot.getIngredient().getId() : null)
                                        .ingredientName(snapshot.getIngredientNameSnapshot())
                                        .quantityBase(snapshot.getQuantityBase())
                                        .baseUnit(snapshot.getBaseUnit())
                                        .unitPriceSnapshot(snapshot.getUnitPriceSnapshot())
                                        .lineTotal(snapshot.getLineTotal())
                                        .build())
                                .collect(Collectors.toList())
                                : List.of())
                        .build())
                .collect(Collectors.toList())
                : new ArrayList<>();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userEmail(order.getUser() != null ? order.getUser().getEmail() : null)
                .userFullName(order.getUser() != null ? order.getUser().getFullName() : null)
                .status(order.getStatus() != null ? order.getStatus().name() : "pending")
                .deliveryAddressText(order.getDeliveryAddressText())
                .deliveryPhone(order.getDeliveryPhone())
                .note(order.getNote())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items)
                .build();
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private BigDecimal scaleMoney(BigDecimal value) {
        return zeroIfNull(value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleQuantity(BigDecimal value) {
        return zeroIfNull(value).setScale(3, RoundingMode.HALF_UP);
    }

    private record PendingOrderItem(
            Recipe recipe,
            int servings,
            List<PendingIngredientSelection> ingredientSelections
    ) {
    }

    private record PendingIngredientSelection(
            String ingredientId,
            BigDecimal quantity,
            String unit
    ) {
    }

    private record PreparedOrder(
            List<PreparedOrderItem> items,
            BigDecimal totalAmount
    ) {
    }

    private record PreparedOrderItem(
            Recipe recipe,
            int servings,
            BigDecimal pricePerServingSnapshot,
            BigDecimal lineTotal,
            List<PreparedIngredientSnapshot> ingredientSnapshots
    ) {
    }

    private record PreparedIngredientSnapshot(
            Ingredient ingredient,
            BigDecimal quantityBase,
            BigDecimal unitPriceSnapshot,
            BigDecimal lineTotal
    ) {
    }
}
