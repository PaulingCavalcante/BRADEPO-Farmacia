-- ============================================================================
-- BRADEPO-Farmacia — dump do banco populado (entregável do Projeto 2)
--
-- COMO IMPORTAR:
--   mysql -u root -p < db/farmacia-dump.sql
-- (cria o schema `farmacia`, as tabelas e os dados de exemplo já preenchidos)
--
-- Este dump representa o banco DEPOIS de algumas vendas reais, para o professor
-- ver "coisa funcionando" sem precisar subir a aplicação. As mesmas tabelas são
-- criadas pelo Hibernate (ddl-auto=update) ao subir a app; os tipos abaixo
-- espelham os gerados por ele (Spring Boot 3.2 / Hibernate 6).
--
-- Para REGERAR um dump real a partir do seu MySQL populado:
--   mysqldump -u root -p --databases farmacia > db/farmacia-dump.sql
-- ============================================================================

CREATE DATABASE IF NOT EXISTS farmacia
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE farmacia;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- Tabela: produto
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS produto;
CREATE TABLE produto (
  id          bigint        NOT NULL AUTO_INCREMENT,
  nome        varchar(255)  NOT NULL,
  categoria   varchar(255)  NOT NULL,
  preco       decimal(10,2) NOT NULL,
  estoque     integer       NOT NULL,
  controlado  bit(1)        NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_produto_nome (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- estoque já reflete as vendas registradas abaixo
INSERT INTO produto (id, nome, categoria, preco, estoque, controlado) VALUES
  (1,  'Dipirona',          'MEDICAMENTO', 12.90,  98, 0),
  (2,  'Paracetamol',       'MEDICAMENTO',  9.50,  79, 0),
  (3,  'Amoxicilina',       'MEDICAMENTO', 25.00,  40, 0),
  (4,  'Omeprazol',         'MEDICAMENTO', 18.00,  60, 0),
  (5,  'Rivotril',          'MEDICAMENTO', 35.00,  19, 1),
  (6,  'Diazepam',          'MEDICAMENTO', 28.00,  15, 1),
  (7,  'Ritalina',          'MEDICAMENTO', 60.00,   9, 1),
  (8,  'Shampoo Anticaspa', 'HIGIENE',     22.00,  50, 0),
  (9,  'Sabonete',          'HIGIENE',      3.50, 199, 0),
  (10, 'Pasta de Dente',    'HIGIENE',      7.00, 150, 0),
  (11, 'Ibuprofeno',        'MEDICAMENTO', 15.00,   0, 0);

-- ----------------------------------------------------------------------------
-- Tabela: cliente
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS cliente;
CREATE TABLE cliente (
  id        bigint       NOT NULL AUTO_INCREMENT,
  cpf       varchar(255) NOT NULL,
  nome      varchar(255) NOT NULL,
  idoso     bit(1)       NOT NULL,
  convenio  bit(1)       NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_cliente_cpf (cpf)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO cliente (id, cpf, nome, idoso, convenio) VALUES
  (1, '529.982.247-25', 'Maria Souza',  1, 1),
  (2, '111.444.777-35', 'Joao Pereira', 0, 0),
  (3, '390.533.447-05', 'Ana Lima',     1, 0);

-- ----------------------------------------------------------------------------
-- Tabela: venda
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS venda;
CREATE TABLE venda (
  id                   bigint        NOT NULL AUTO_INCREMENT,
  status               varchar(255),
  cpf                  varchar(255),
  produto              varchar(255),
  nota_id              varchar(255),
  protocolo_sefaz      varchar(255),
  protocolo_ans        varchar(255),
  motivo               varchar(255),
  data_hora            datetime(6),
  valor_bruto          decimal(10,2),
  percentual_desconto  decimal(10,2),
  valor_desconto       decimal(10,2),
  valor_liquido        decimal(10,2),
  descricao_desconto   varchar(255),
  canal                varchar(255),
  vendedor             varchar(255),
  comissao             decimal(10,2),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Vendas de exemplo cobrindo: internet/balcao, comum/controlado,
-- cliente cadastrado (idoso+convenio, adulto, idoso sem convenio) e avulso.
INSERT INTO venda
  (id, status, cpf, produto, nota_id, protocolo_sefaz, protocolo_ans, motivo, data_hora,
   valor_bruto, percentual_desconto, valor_desconto, valor_liquido, descricao_desconto,
   canal, vendedor, comissao) VALUES
  (1, 'AUTORIZADA', '529.982.247-25', 'Dipirona', 'NF-0001', 'SEFAZ-1718881500000', NULL, NULL,
   '2026-06-20 10:15:00.000000', 12.90, 15.00, 1.94, 10.96,
   'Desconto convenio (idoso): 15% (maior vantagem)', 'INTERNET', NULL, NULL),

  (2, 'AUTORIZADA', '111.444.777-35', 'Rivotril', 'NF-0002', 'SEFAZ-1718968000000', 'ANS-1718968000100', NULL,
   '2026-06-21 14:30:00.000000', 35.00, 5.00, 1.75, 33.25,
   'Desconto fabricante (cliente cadastrado): 5%', 'INTERNET', NULL, NULL),

  (3, 'AUTORIZADA', NULL, 'Sabonete', 'NF-0003', 'SEFAZ-1719054500000', NULL, NULL,
   '2026-06-22 09:05:00.000000', 3.50, 0.00, 0.00, 3.50,
   'Sem desconto (cliente nao cadastrado)', 'BALCAO', 'Carlos', 0.18),

  (4, 'AUTORIZADA', '390.533.447-05', 'Ritalina', 'NF-0004', 'SEFAZ-1719141000000', 'ANS-1719141000100', NULL,
   '2026-06-23 16:45:00.000000', 60.00, 8.00, 4.80, 55.20,
   'Desconto fabricante (cliente cadastrado): 8%', 'BALCAO', 'Carlos', 2.76),

  (5, 'AUTORIZADA', '111.444.777-35', 'Paracetamol', 'NF-0005', 'SEFAZ-1719227500000', NULL, NULL,
   '2026-06-24 11:20:00.000000', 9.50, 5.00, 0.48, 9.02,
   'Desconto fabricante (cliente cadastrado): 5%', 'INTERNET', NULL, NULL),

  (6, 'AUTORIZADA', '529.982.247-25', 'Dipirona', 'NF-0006', 'SEFAZ-1719300000000', NULL, NULL,
   '2026-06-25 08:30:00.000000', 12.90, 15.00, 1.94, 10.96,
   'Desconto convenio (idoso): 15% (maior vantagem)', 'INTERNET', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
