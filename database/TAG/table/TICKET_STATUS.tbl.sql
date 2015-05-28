

CREATE SEQUENCE ticket_status_id_seq start 1
    increment 1
    NO MAXVALUE
    CACHE 1;		
	
--DROP TABLE TICKET_STATUS;
	
CREATE TABLE TICKET_STATUS
(
ID				INT NOT NULL DEFAULT nextval('ticket_status_id_seq'),
STATUS				VARCHAR(30),
DESCRIPTION			VARCHAR(30)
);
ALTER SEQUENCE ticket_status_id_seq OWNED BY TICKET_STATUS.ID;	