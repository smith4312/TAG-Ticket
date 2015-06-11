
DROP FUNCTION IF EXISTS validateAgent(varchar(30), varchar(30));


CREATE OR REPLACE FUNCTION validateAgent( userName varchar(30), pswd varchar(30)) RETURNS boolean AS $$

BEGIN
	IF EXISTS( SELECT 1 FROM AGENT WHERE user_name =userName AND password = pswd ) THEN
		RETURN true;
	ELSE
		RETURN false;
END IF;
END;
$$ LANGUAGE plpgsql;


--select validateAgent('DSMITH', 'Password');
--select validateAgent('DSMITH', 'Password1');
