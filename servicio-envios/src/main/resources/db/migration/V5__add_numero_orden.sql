ALTER TABLE envios ADD COLUMN numero_orden VARCHAR(20);

-- Update existing records to have a random order number so the column can eventually be NOT NULL if desired.
UPDATE envios SET numero_orden = FLOOR(RANDOM() * 90000000 + 10000000)::VARCHAR WHERE numero_orden IS NULL;

-- Make it unique (optional, but highly recommended for order numbers)
ALTER TABLE envios ADD CONSTRAINT uq_numero_orden UNIQUE (numero_orden);
