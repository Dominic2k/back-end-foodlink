package org.datpham.foodlink.util;

import org.datpham.foodlink.entity.Ingredient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class IngredientUnitSupport {

    private IngredientUnitSupport() {
    }

    public static String normalizeUnit(String unit) {
        if (unit == null) {
            return "";
        }

        return switch (unit.trim().toLowerCase(Locale.ROOT)) {
            case "g", "gram", "grams" -> "g";
            case "kg", "kilogram", "kilograms" -> "kg";
            case "mg", "milligram", "milligrams" -> "mg";
            case "ml", "milliliter", "milliliters" -> "ml";
            case "l", "liter", "liters" -> "l";
            case "piece", "pieces", "pc", "pcs", "unit", "units", "qua" -> "piece";
            default -> unit.trim().toLowerCase(Locale.ROOT);
        };
    }

    public static BigDecimal convert(BigDecimal quantity, String fromUnit, String toUnit) {
        if (quantity == null) {
            return null;
        }

        String source = normalizeUnit(fromUnit);
        String target = normalizeUnit(toUnit);

        if (target.isBlank()) {
            return quantity;
        }
        if (source.isBlank()) {
            return null;
        }
        if (source.equals(target)) {
            return quantity;
        }

        BigDecimal sourceWeight = toGrams(quantity, source);
        BigDecimal targetWeight = unitToGrams(target);
        if (sourceWeight != null && targetWeight != null) {
            return sourceWeight.divide(targetWeight, 6, RoundingMode.HALF_UP);
        }

        BigDecimal sourceVolume = toMilliliters(quantity, source);
        BigDecimal targetVolume = unitToMilliliters(target);
        if (sourceVolume != null && targetVolume != null) {
            return sourceVolume.divide(targetVolume, 6, RoundingMode.HALF_UP);
        }

        if (isCountUnit(source) && isCountUnit(target)) {
            return quantity;
        }

        return null;
    }

    public static BigDecimal convertToBaseQuantity(Ingredient ingredient, BigDecimal quantity, String quantityUnit) {
        if (ingredient == null) {
            return null;
        }
        return convert(quantity, quantityUnit, ingredient.getBaseUnit());
    }

    public static BigDecimal calculateLinePriceFromRequestUnit(Ingredient ingredient, BigDecimal quantity, String quantityUnit) {
        BigDecimal quantityBase = convertToBaseQuantity(ingredient, quantity, quantityUnit);
        return calculateLinePriceFromBaseQuantity(ingredient, quantityBase);
    }

    public static BigDecimal calculateLinePriceFromBaseQuantity(Ingredient ingredient, BigDecimal quantityBase) {
        if (ingredient == null || ingredient.getPricePerBaseUnit() == null || quantityBase == null) {
            return null;
        }
        return ingredient.getPricePerBaseUnit().multiply(quantityBase);
    }

    public static boolean isWeightUnit(String unit) {
        return unitToGrams(normalizeUnit(unit)) != null;
    }

    public static boolean isVolumeUnit(String unit) {
        return unitToMilliliters(normalizeUnit(unit)) != null;
    }

    public static boolean isCountUnit(String unit) {
        return "piece".equals(normalizeUnit(unit));
    }

    private static BigDecimal toGrams(BigDecimal quantity, String unit) {
        BigDecimal factor = unitToGrams(unit);
        if (factor == null) {
            return null;
        }
        return quantity.multiply(factor);
    }

    private static BigDecimal unitToGrams(String unit) {
        return switch (normalizeUnit(unit)) {
            case "g" -> BigDecimal.ONE;
            case "kg" -> new BigDecimal("1000");
            case "mg" -> new BigDecimal("0.001");
            default -> null;
        };
    }

    private static BigDecimal toMilliliters(BigDecimal quantity, String unit) {
        BigDecimal factor = unitToMilliliters(unit);
        if (factor == null) {
            return null;
        }
        return quantity.multiply(factor);
    }

    private static BigDecimal unitToMilliliters(String unit) {
        return switch (normalizeUnit(unit)) {
            case "ml" -> BigDecimal.ONE;
            case "l" -> new BigDecimal("1000");
            default -> null;
        };
    }
}
