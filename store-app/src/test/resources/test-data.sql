-- Тесты сами наполняют БД; файл нужен, чтобы spring.sql.init.data-locations находил ресурс.
DELETE FROM order_items WHERE 1 = 0;
DELETE FROM orders WHERE 1 = 0;
DELETE FROM cart_items WHERE 1 = 0;
DELETE FROM items WHERE 1 = 0;
