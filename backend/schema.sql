-- SpendCheck Database Initialization Script (PostgreSQL)
-- If creating the database manually in psql:
--   CREATE DATABASE spendcheck;
--
-- Then connect to spendcheck database:
--   \c spendcheck;

CREATE TABLE IF NOT EXISTS expenses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    category VARCHAR(32) NOT NULL,
    expense_date DATE NOT NULL,
    notes VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_expenses_category ON expenses(category);
CREATE INDEX IF NOT EXISTS idx_expenses_date ON expenses(expense_date);
