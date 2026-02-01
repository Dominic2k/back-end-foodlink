# FoodLink Backend

API backend cho dự án FoodLink, xây dựng với Node.js và Express.

## 🚀 Bắt đầu

Hướng dẫn này sẽ giúp bạn cài đặt và chạy source code backend trên máy cá nhân để bắt đầu phát triển.

### 📋 Yêu cầu cài đặt

Trước khi bắt đầu, hãy đảm bảo bạn đã cài đặt các công cụ sau:

*   [Node.js](https://nodejs.org/) (phiên bản LTS được khuyến nghị)
*   [Yarn](https://yarnpkg.com/) hoặc [npm](https://www.npmjs.com/)
*   Một hệ quản trị cơ sở dữ liệu (ví dụ: MongoDB, PostgreSQL, MySQL)

### 🔧 Cài đặt

1.  **Clone repository**
    ```sh
    git clone <your-backend-repo-url>
    cd foodlink-backend
    ```

2.  **Cài đặt dependencies**
    Sử dụng npm:
    ```sh
    npm install
    ```
    Hoặc sử dụng Yarn:
    ```sh
    yarn install
    ```

3.  **Cấu hình biến môi trường**
    Tạo một file `.env` từ file `.env.example`:
    ```sh
    cp .env.example .env
    ```
    Sau đó, mở file `.env` và điền các giá trị cần thiết, ví dụ:
    ```
    PORT=8080
    DATABASE_URL="your_database_connection_string"
    JWT_SECRET="your_jwt_secret"
    ```

### 🏃 Chạy dự án

Để khởi động server ở chế độ phát triển (với hot-reload):

```sh
npm run dev
