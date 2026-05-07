TRUNCATE TABLE order_items, orders, cart_items, app_users, items
RESTART IDENTITY CASCADE;

INSERT INTO items (title, description, img_path, price) VALUES
('Футбольный мяч', 'Прочный мяч для тренировок и любительских матчей.', '/images/ball.png', 2500),
('Кружка', 'Керамическая кружка для горячих напитков.', '/images/mug.png', 700),
('Механическая клавиатура', 'Проводная клавиатура с подсветкой.', '/images/keyboard.png', 6500),
('Компьютерная мышь', 'Оптическая мышь для работы и дома.', '/images/mouse.png', 1800),
('Настольная лампа', 'Светодиодная лампа для рабочего стола.', '/images/lamp.png', 3200),
('Беспроводные наушники', 'Наушники с Bluetooth и шумоподавлением.', '/images/headphones.png', 8900),
('Рюкзак', 'Городской рюкзак для ноутбука и повседневных вещей.', '/images/backpack.png', 4700),
('Термос', 'Металлический термос объёмом 500 мл.', '/images/thermos.png', 1900);

INSERT INTO app_users (username, password, enabled) VALUES
('user1', '$2a$10$psorxjrC7pT9mVt0irwcDu1GULYUYyrPjyLcU19flDXCelT0E0wXS', true),
('user2', '$2a$10$psorxjrC7pT9mVt0irwcDu1GULYUYyrPjyLcU19flDXCelT0E0wXS', true);