-- ============================================================================
-- Seed inicial da farmácia. Roda no startup (spring.sql.init.mode=always),
-- depois que o Hibernate cria as tabelas (defer-datasource-initialization=true).
-- INSERT IGNORE => idempotente: se o nome (UNIQUE) já existe, a linha é ignorada,
-- então rodar a app várias vezes não duplica os dados.
-- ============================================================================

-- Produtos: medicamentos comuns, controlados (controlado=1) e itens de higiene.
INSERT IGNORE INTO produto (nome, categoria, preco, estoque, controlado) VALUES
  ('Dipirona',          'MEDICAMENTO', 12.90, 100, 0),
  ('Paracetamol',       'MEDICAMENTO',  9.50,  80, 0),
  ('Amoxicilina',       'MEDICAMENTO', 25.00,  40, 0),
  ('Omeprazol',         'MEDICAMENTO', 18.00,  60, 0),
  ('Rivotril',          'MEDICAMENTO', 35.00,  20, 1),
  ('Diazepam',          'MEDICAMENTO', 28.00,  15, 1),
  ('Ritalina',          'MEDICAMENTO', 60.00,  10, 1),
  ('Shampoo Anticaspa', 'HIGIENE',     22.00,  50, 0),
  ('Sabonete',          'HIGIENE',      3.50, 200, 0),
  ('Pasta de Dente',    'HIGIENE',      7.00, 150, 0),
  ('Ibuprofeno',        'MEDICAMENTO', 15.00,   0, 0);

-- Clientes cadastrados (Fase 3/4): combinações para demonstrar os descontos.
--  - Maria: idosa COM convênio  -> ganha a maior vantagem (convênio x fabricante)
--  - João: adulto sem convênio  -> só o desconto progressivo de cadastrado
--  - Ana: idosa sem convênio     -> só o progressivo (convênio não se aplica)
INSERT IGNORE INTO cliente (cpf, nome, idoso, convenio) VALUES
  ('529.982.247-25', 'Maria Souza',    1, 1),
  ('111.444.777-35', 'Joao Pereira',   0, 0),
  ('390.533.447-05', 'Ana Lima',       1, 0);
