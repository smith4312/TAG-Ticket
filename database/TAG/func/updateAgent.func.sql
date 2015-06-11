
DROP FUNCTION IF EXISTS updateAgent (	agentId integer,
					new_first_name varchar(30),
					new_last_name varchar(30),
					new_user_name varchar(30),
					new_password varchar(30),
					new_address varchar(30),
					new_city varchar(30),
					new_state_cd varchar(6),
					new_zip  varchar(10),
					new_country varchar(20),
					new_email varchar(60),
					new_phone  varchar(20),
					new_mobile_phone varchar(20),
					new_notes varchar(255),
					new_office_location_id integer );


CREATE OR REPLACE FUNCTION updateAgent(	agentId integer,
					new_first_name varchar(30),
					new_last_name varchar(30),
					new_user_name varchar(30),
					new_password varchar(30),
					new_address varchar(30),
					new_city varchar(30),
					new_state_cd varchar(6),
					new_zip  varchar(10),
					new_country varchar(20),
					new_email varchar(60),
					new_phone  varchar(20),
					new_mobile_phone varchar(20),
					new_notes varchar(255),
					new_office_location_id integer ) RETURNS text AS $$
BEGIN
	--validation
	--if username is taken


	IF NOT EXISTS( SELECT 1 FROM AGENT WHERE id = agentId) THEN
		RETURN 'Agent not found';
	ELSE 
	UPDATE AGENT SET 	first_name = new_first_name,
				last_name = new_last_name,
				user_name = new_user_name,
				password = new_password,
				address = new_address,
				city	= new_city,
				state_cd = new_state_cd,
				zip = new_zip,
				country = new_country,
				email = new_email,
				phone = new_phone,
				mobile_phone = new_mobile_phone,
				notes = new_notes,
				office_location_id = new_office_location_id
	WHERE id = agentId;
	RETURN 'Success'; 
	END IF;
END;
$$ LANGUAGE plpgsql;		

--  select id from AGENT where user_name = 'DSMITH'
/*
 select updateAgent( 1, 'David', 'Smith', 'DSMITH', 'Password', '123 Main. St.', 'Lakewood', 'NJ', '08701', 'USA',
	'abcdef@gmail.com', '(732) 123-4567', '(732) 123-4567', 'keep everything the same, but update the comment', 1 );
*/