DROP TABLE IF EXISTS dev.Attribute ;

-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE IF NOT EXISTS dev.Attribute (
  idAttribute SERIAL NOT NULL,
  idUml VARCHAR(45) NULL,
  idClass INT NULL,
  name VARCHAR(45) NULL,
  idType INT NULL,
  visibility VARCHAR(45) NULL,
  PRIMARY KEY (idAttribute),
  CONSTRAINT fk_Attribute_1
    FOREIGN KEY (idClass)
    REFERENCES dev.Class (idClass)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_Attribute_2
    FOREIGN KEY (idType)
    REFERENCES dev.Type (idType)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
;

-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE INDEX fk_Attribute_1_idx ON dev.Attribute (idClass ASC);

-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE INDEX fk_Attribute_2_idx ON dev.Attribute (idType ASC);