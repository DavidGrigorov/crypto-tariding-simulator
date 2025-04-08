CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    balance DECIMAL(20, 2) NOT NULL DEFAULT 10000.00
);

CREATE TABLE crypto_assets (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    symbol VARCHAR(10) NOT NULL,
    amount DECIMAL(20, 8) NOT NULL,
    UNIQUE(user_id, symbol)
);

CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    symbol VARCHAR(10) NOT NULL,
    transaction_type VARCHAR(4) NOT NULL,
    amount DECIMAL(20, 8) NOT NULL,
    price DECIMAL(20, 2) NOT NULL,
    total_value DECIMAL(20, 2) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);