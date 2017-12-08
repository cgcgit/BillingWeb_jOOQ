--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.5
-- Dumped by pg_dump version 9.6.5

-- Started on 2017-11-01 14:51:48 CET

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET SESSION AUTHORIZATION 'billing_admin';

DROP DATABASE db_billing;
--
-- TOC entry 3162 (class 1262 OID 16386)
-- Name: db_billing; Type: DATABASE; Schema: -; Owner: billing_admin
--

CREATE DATABASE db_billing WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'es_ES.UTF-8' LC_CTYPE = 'es_ES.UTF-8';


\connect db_billing

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 5 (class 2615 OID 18989)
-- Name: billing; Type: SCHEMA; Schema: -; Owner: billing_admin
--

CREATE SCHEMA billing;


SET SESSION AUTHORIZATION DEFAULT;

--
-- TOC entry 1 (class 3079 OID 12469)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 3166 (class 0 OID 0)
-- Dependencies: 1
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- TOC entry 2 (class 3079 OID 20291)
-- Name: pldbgapi; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS pldbgapi WITH SCHEMA public;


--
-- TOC entry 3167 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pldbgapi; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pldbgapi IS 'server-side support for debugging PL/pgSQL functions';


SET SESSION AUTHORIZATION 'billing_admin';

SET search_path = billing, pg_catalog;

--
-- TOC entry 333 (class 1255 OID 18990)
-- Name: tf_add_product_type_validation(); Type: FUNCTION; Schema: billing; Owner: billing_admin
--

CREATE FUNCTION tf_add_product_type_validation() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
	DECLARE
	  product_type_exists INTEGER;
	BEGIN
	  SELECT 1
            INTO product_type_exists
	     FROM billing.mt_product_type
            WHERE product_type_code = NEW.product_type_code
              AND ((NEW.start_date BETWEEN start_date AND end_date) 
		   OR (NEW.end_date BETWEEN start_date AND end_date));
          IF NOT FOUND 
	   THEN
	     RETURN NEW;
	   ELSE
             RAISE EXCEPTION 'The product type with this product_type_code exists for these dates';
	  END IF;
	END;
 $$;


--
-- TOC entry 334 (class 1255 OID 18991)
-- Name: tf_add_service_type_validation(); Type: FUNCTION; Schema: billing; Owner: billing_admin
--

CREATE FUNCTION tf_add_service_type_validation() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
	DECLARE
	  service_type_exists INTEGER;
	BEGIN
	  SELECT 1
            INTO service_type_exists
	     FROM billing.mt_service_type
            WHERE service_type_code = NEW.service_type_code
              AND ((NEW.start_date BETWEEN start_date AND end_date) 
		   OR (NEW.end_date BETWEEN start_date AND end_date));
          IF NOT FOUND 
	   THEN
	     RETURN NEW;
	   ELSE
             RAISE EXCEPTION 'The product type with this service_type_code exists for these dates';
	  END IF;
	END;
 $$;


--
-- TOC entry 335 (class 1255 OID 18992)
-- Name: tf_add_user_validation(); Type: FUNCTION; Schema: billing; Owner: billing_admin
--

CREATE FUNCTION tf_add_user_validation() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
	DECLARE
	  user_exists INTEGER;
	BEGIN
	  SELECT 1
            INTO user_exists
	     FROM billing.it_user
            WHERE user_code = NEW.user_code
              AND ((NEW.start_date BETWEEN start_date AND end_date) 
		   OR (NEW.end_date BETWEEN start_date AND end_date));
          IF NOT FOUND 
	   THEN
	     RETURN NEW;
	   ELSE
             RAISE EXCEPTION 'The user with this user_code exists for these dates';
	  END IF;
	END;
 $$;


--
-- TOC entry 330 (class 1255 OID 18993)
-- Name: tf_format_default_dates(); Type: FUNCTION; Schema: billing; Owner: billing_admin
--

CREATE FUNCTION tf_format_default_dates() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
	BEGIN
	NEW.start_date := to_timestamp (to_char(NEW.start_date, 'DD/MM/YYYY') || ' ' || '00:00:00', 'DD/MM/YYYY HH24:MI:SS');
	NEW.end_date := to_timestamp (to_char(NEW.end_date, 'DD/MM/YYYY') || ' ' || '23:59:59', 'DD/MM/YYYY HH24:MI:SS');
    	RETURN NEW;
	END;
 $$;


--
-- TOC entry 329 (class 1255 OID 18994)
-- Name: tf_product_type_align_records(timestamp without time zone, timestamp without time zone, integer, character varying, timestamp without time zone); Type: FUNCTION; Schema: billing; Owner: billing_admin
--

CREATE FUNCTION tf_product_type_align_records(i_start_date timestamp without time zone, i_end_date timestamp without time zone, i_product_type_id integer, i_modif_user character varying, i_modif_date timestamp without time zone) RETURNS integer
    LANGUAGE plpgsql
    AS $$/**
  DESCRIPTION:
    Check if the mt_product_type table has records aligned with given dates. If not, it will insert / update records for alignment.
  RETURN:
    0 - No records to modify
    1 - Some records to modify
  
**/
	DECLARE
	  
	  minSD TIMESTAMP;
	  maxED TIMESTAMP;
	  
	  prevED TIMESTAMP;
	  postSD TIMESTAMP;
	  
	  closestSDtoStartDate TIMESTAMP;
	  closestEDtoStartDate TIMESTAMP;
	  closestSDtoEndDate TIMESTAMP;
          closestEDtoEndDate TIMESTAMP;
          
          row1 billing.mt_product_type%ROWTYPE;
          row2 billing.mt_product_type%ROWTYPE;
          
          
	BEGIN
	
	 -- The giving i_start_date must be less than giving i_end_date
	 IF (i_start_date>=i_end_date)
	   THEN
	     RAISE EXCEPTION 'The giving dates are incorrect. Start date must be less than end date';
	 END IF;
	
	  -- Get the minimum start_date and the maximum end_date of the mt_product_type table for the giving product_type_id
	  SELECT MIN(start_date), MAX(end_date)
	    INTO minSD, maxED
	    FROM billing.mt_product_type
           WHERE product_type_id = i_product_type_id;
           
           IF NOT FOUND 
	    THEN	     
              RAISE EXCEPTION 'The giving product_type_id was not found';
	  END IF;
	  
	  --The giving i_start_date and i_end_date must be in the interval of the minimun start_date and the maximum end_date of the product_type	  
	  IF (i_start_date <= minSD or i_end_date >= maxED)
	    THEN
	      RAISE EXCEPTION 'The giving dates are not included into the billing.product_type table for the giving product_type_id';
	  END IF;
	  
	  -- Get the closets start_date and end_date of the product type for the given i_start_date
	  SELECT MAX(start_date)
            INTO closestSDtoStartDate
	    FROM billing.mt_product_type
           WHERE product_type_id = i_product_type_id
             AND start_date <= i_start_date;
             
          IF NOT FOUND 
	    THEN	     
              RAISE EXCEPTION 'MAX(start_date) shorter than the given start date not found';
	  END IF;              
                    
          SELECT MIN(end_date)
            INTO closestEDtoStartDate
	    FROM billing.mt_product_type
           WHERE product_type_id = i_product_type_id
             AND end_date >= i_start_date;  
             
          IF NOT FOUND 
	    THEN	     
              RAISE EXCEPTION 'MIN(end_date) greather than the given start date not found';
	  END IF;

	  -- Get the closets start_date and end_date of the product type for the given i_end_date
	  SELECT MAX(start_date)
            INTO closestSDtoEndDate
	    FROM billing.mt_product_type
           WHERE product_type_id = i_product_type_id
             AND start_date <= i_end_date;
             
          IF NOT FOUND 
	    THEN	     
              RAISE EXCEPTION 'MAX(start_date) shorter than the given end date not found';
	  END IF;              
          
          SELECT MIN(end_date)
            INTO closestEDtoEndDate
	    FROM billing.mt_product_type
           WHERE product_type_id = i_product_type_id
             AND end_date >= i_end_date;  
             
          IF NOT FOUND 
	    THEN	     
              RAISE EXCEPTION 'MIN(end_date) greather than the given end date not found';
	  END IF;
	  
           -- Get the previous day for the i_start_date (it would be an end date: dd/mm/yyyy 23:59:59)
           -- and the next day for the i_end_date (it would be a start date: dd/mm/yyyy 00:00:00)
           SELECT date(i_start_date) - interval '1 second', 
                  date(i_end_date) + interval '1 day'             
             INTO prevED, 
                  postSD;
                  
          IF ((closestSDtoStartDate = i_start_date or closestSDtoEndDate = i_start_date) and
              (closestEDtoStartDate = i_end_date or closestEDtoEndDate = i_end_date))
            THEN -- records are alignment --> do nothing              
              RETURN 0;
          END IF;

          
          IF (closestSDtoStartDate = i_start_date)
            THEN
              -- Get the records with start_date = closestSDtoEndDate and end_date = closestEDtoEndDate
              SELECT * 
                INTO row1
                FROM billing.mt_product_type  
               WHERE product_type_id = i_product_type_id
                 AND start_date= closestSDtoEndDate;
              SELECT * 
                INTO row2
                FROM billing.mt_product_type  
               WHERE product_type_id = i_product_type_id
                 AND end_date= closestEDtoEndDate;
                 
              -- Update the first record setting end_date = i_end_date                 
              UPDATE billing.mt_product_type 
                 SET end_date = i_end_date,
                     modif_user=i_modif_user,
                     modif_date=i_modif_date
               WHERE product_type_id = i_product_type_id
                 AND start_date= closestSDtoEndDate;
                 
              -- Insert a new record with all the values equals to the second record, exces i_start_date, input_user, input_date, modif_user and modif_date
              row2.start_date=postSD;
              row2.input_user=i_modif_user;
              row2.input_date=i_modif_date;     
              row2.modif_user=NULL;
              row2.modif_date=NULL;  
              INSERT INTO billing.mt_product_type VALUES(row2.*);   
              RETURN 1;
            ELSE
              IF (closestEDtoEndDate = i_end_date)
                THEN
                  -- Get the records with start_date = closestSDtoStartDate and end_date = closestEDtoStartDate
                  SELECT * 
                    INTO row1
                    FROM billing.mt_product_type  
                   WHERE product_type_id = i_product_type_id
                     AND start_date= closestSDtoStartDate;
                  SELECT * 
                    INTO row2
                    FROM billing.mt_product_type  
                   WHERE product_type_id = i_product_type_id
                     AND end_date= closestEDtoStartDate;
                 
                  -- Update the first record setting end_date = prevED                 
                  UPDATE billing.mt_product_type 
                     SET end_date = prevED,
                         modif_user=i_modif_user,
                         modif_date=i_modif_date
                   WHERE product_type_id = i_product_type_id
                     AND start_date= closestSDtoStartDate;
                 
                  -- Insert a new record with all the values equals to the second record, exces i_start_date, input_user, input_date, modif_user and modif_date
                  row2.start_date=i_start_date;
                  row2.input_user=i_modif_user;
                  row2.input_date=i_modif_date;     
                  row2.modif_user=NULL;
                  row2.modif_date=NULL;  
                  INSERT INTO billing.mt_product_type VALUES(row2.*);   
                  RETURN 1;
                ELSE
                  IF (closestSDtoStartDate=closestSDtoEndDate)
                    THEN -- One record to modify
                      -- Get the records with start_date = closestSDtoStartDate and end_date = closestEDtoStartDate
                      SELECT * 
                        INTO row1
                        FROM billing.mt_product_type  
                       WHERE product_type_id = i_product_type_id
                         AND start_date= closestSDtoStartDate;
                      SELECT * 
                        INTO row2
                        FROM billing.mt_product_type  
                       WHERE product_type_id = i_product_type_id
                         AND end_date= closestEDtoStartDate;
                  
                      -- Update the first record setting end_date = prevED                 
                      UPDATE billing.mt_product_type 
                         SET end_date = prevED,
                             modif_user=i_modif_user,
                             modif_date=i_modif_date
                       WHERE product_type_id = i_product_type_id
                         AND start_date= closestSDtoStartDate;
                 
                      -- Insert a new record with all the values equals to the first record, exces i_start_date, i_end_date, input_user, input_date, modif_user and modif_date
                      row1.start_date=i_start_date;
                      row1.end_date=i_end_date;
                      row1.input_user=i_modif_user;
                      row1.input_date=i_modif_date;     
                      row1.modif_user=NULL;
                      row1.modif_date=NULL;  
                      INSERT INTO billing.mt_product_type VALUES(row1.*);  
                  
                      -- Insert a new record with all the values equals to the second record, exces i_start_date, input_user, input_date, modif_user and modif_date
                      row2.start_date=postSD;
                      row2.input_user=i_modif_user;
                      row2.input_date=i_modif_date;     
                      row2.modif_user=NULL;
                      row2.modif_date=NULL;  
                      INSERT INTO billing.mt_product_type VALUES (row2.*);   
                      RETURN 1;
                    ELSE -- More than one record to modify
                      -- Get the records with start_date = closestSDtoStartDate and end_date = closestEDtoEndDate
                      SELECT * 
                        INTO row1
                        FROM billing.mt_product_type  
                       WHERE product_type_id = i_product_type_id
                         AND start_date= closestSDtoStartDate;
                      SELECT * 
                        INTO row2
                        FROM billing.mt_product_type  
                       WHERE product_type_id = i_product_type_id
                         AND end_date= closestEDtoEndDate;
                  
                      -- Update the first record setting end_date = prevED                 
                      UPDATE billing.mt_product_type 
                         SET end_date = prevED,
                             modif_user=i_modif_user,
                             modif_date=i_modif_date
                       WHERE product_type_id = i_product_type_id
                         AND start_date= closestSDtoStartDate;
                 
                      -- Insert a new record with all the values equals to the first record, exces i_start_date, input_user, input_date, modif_user and modif_date
                      row1.start_date=i_start_date;                      
                      row1.input_user=i_modif_user;
                      row1.input_date=i_modif_date;     
                      row1.modif_user=NULL;
                      row1.modif_date=NULL;  
                      INSERT INTO billing.mt_product_type VALUES (row1.*);  
                  
                      -- Update the second record setting end_date = i_end_date                 
                      UPDATE billing.mt_product_type 
                         SET end_date = i_end_date,
                             modif_user=i_modif_user,
                             modif_date=i_modif_date
                       WHERE product_type_id = i_product_type_id
                         AND start_date= closestEDtoEndDate;
                  
                      -- Insert a new record with all the values equals to the second record, exces i_start_date, input_user, input_date, modif_user and modif_date
                      row2.start_date=postSD;
                      row2.input_user=i_modif_user;
                      row2.input_date=i_modif_date;     
                      row2.modif_user=NULL;
                      row2.modif_date=NULL;  
                      INSERT INTO billing.mt_product_type VALUES (row2.*); 
                      RETURN 1;
                  END IF;
              END IF;
          END IF;
        END;        
 $$;


--
-- TOC entry 336 (class 1255 OID 18996)
-- Name: tf_status_entity_type_validation(); Type: FUNCTION; Schema: billing; Owner: billing_admin
--

CREATE FUNCTION tf_status_entity_type_validation() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
	DECLARE
	  status_entity_type_exists INTEGER;
	BEGIN
	  SELECT 1
            INTO status_entity_type_exists
	     FROM billing.mt_status
            WHERE status_id = NEW.status_id
              AND entity_type_id = NEW.entity_type_id;
          IF FOUND 
	   THEN
	     RETURN NEW;
	   ELSE
             RAISE EXCEPTION 'Invalid values - The status id for this entity type not exists';
	  END IF;
	END;
 $$;


SET search_path = public, pg_catalog;

--
-- TOC entry 332 (class 1255 OID 18997)
-- Name: tf_format_default_dates(); Type: FUNCTION; Schema: public; Owner: billing_admin
--

CREATE FUNCTION tf_format_default_dates() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
	BEGIN
	NEW.start_date := to_timestamp (to_char(NEW.start_date, 'DD/MM/YYYY') || ' ' || '00:00:00', 'DD/MM/YYYY HH24:MI:SS');
	NEW.end_date := to_timestamp (to_char(NEW.end_date, 'DD/MM/YYYY') || ' ' || '23:59:59', 'DD/MM/YYYY HH24:MI:SS');
    	RETURN NEW;
	END;
 $$;


SET search_path = billing, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 187 (class 1259 OID 18998)
-- Name: it_user; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE it_user (
    user_id integer NOT NULL,
    user_code character varying(10) NOT NULL,
    user_name character varying(200) NOT NULL,
    password character(32) NOT NULL,
    profile_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT ('now'::text)::date NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('31-12-9999 23:59:59'::text, 'DD-MM-YYYY HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3174 (class 0 OID 0)
-- Dependencies: 187
-- Name: TABLE it_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE it_user IS 'Instance table of application users';


--
-- TOC entry 3175 (class 0 OID 0)
-- Dependencies: 187
-- Name: COLUMN it_user.user_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN it_user.user_id IS 'Interna identifier of the user';


--
-- TOC entry 3176 (class 0 OID 0)
-- Dependencies: 187
-- Name: COLUMN it_user.user_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN it_user.user_code IS 'Code of the user (to login)';


--
-- TOC entry 3177 (class 0 OID 0)
-- Dependencies: 187
-- Name: COLUMN it_user.user_name; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN it_user.user_name IS 'Complete name of the user - md5 encrypted';


--
-- TOC entry 3178 (class 0 OID 0)
-- Dependencies: 187
-- Name: COLUMN it_user.password; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN it_user.password IS 'Password for the user';


--
-- TOC entry 3179 (class 0 OID 0)
-- Dependencies: 187
-- Name: COLUMN it_user.profile_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN it_user.profile_id IS 'Profile id';


--
-- TOC entry 188 (class 1259 OID 19004)
-- Name: mt_application_level; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_application_level (
    application_level_id integer NOT NULL,
    application_level_code character varying(10) NOT NULL,
    description character varying(100) NOT NULL
);


--
-- TOC entry 3181 (class 0 OID 0)
-- Dependencies: 188
-- Name: TABLE mt_application_level; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_application_level IS 'Master table of application levels of a promotion';


--
-- TOC entry 3182 (class 0 OID 0)
-- Dependencies: 188
-- Name: COLUMN mt_application_level.application_level_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_application_level.application_level_id IS 'Internal identifier of application level';


--
-- TOC entry 3183 (class 0 OID 0)
-- Dependencies: 188
-- Name: COLUMN mt_application_level.application_level_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_application_level.application_level_code IS 'Application level code: PROD-product; SERV-service; EQUI-equipment; PROM-promotion';


--
-- TOC entry 3184 (class 0 OID 0)
-- Dependencies: 188
-- Name: COLUMN mt_application_level.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_application_level.description IS 'Description of application level';


--
-- TOC entry 189 (class 1259 OID 19007)
-- Name: mt_application_level_application_level_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE mt_application_level_application_level_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3186 (class 0 OID 0)
-- Dependencies: 189
-- Name: mt_application_level_application_level_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE mt_application_level_application_level_id_seq OWNED BY mt_application_level.application_level_id;


--
-- TOC entry 190 (class 1259 OID 19009)
-- Name: mt_application_unit; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_application_unit (
    application_unit_id integer NOT NULL,
    application_unit_code character varying(10) NOT NULL,
    description character varying(100) NOT NULL
);


--
-- TOC entry 3188 (class 0 OID 0)
-- Dependencies: 190
-- Name: TABLE mt_application_unit; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_application_unit IS 'Master table of the units on which the promotion applies';


--
-- TOC entry 3189 (class 0 OID 0)
-- Dependencies: 190
-- Name: COLUMN mt_application_unit.application_unit_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_application_unit.application_unit_id IS 'Internal identifier of unit';


--
-- TOC entry 3190 (class 0 OID 0)
-- Dependencies: 190
-- Name: COLUMN mt_application_unit.application_unit_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_application_unit.application_unit_code IS 'Unit code: AMOUNT-amount;MINUTES-minutes;UNITS-units;MB-megabytes';


--
-- TOC entry 3191 (class 0 OID 0)
-- Dependencies: 190
-- Name: COLUMN mt_application_unit.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_application_unit.description IS 'Description of the unit';


--
-- TOC entry 191 (class 1259 OID 19012)
-- Name: mt_application_unit_application_unit_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE mt_application_unit_application_unit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3193 (class 0 OID 0)
-- Dependencies: 191
-- Name: mt_application_unit_application_unit_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE mt_application_unit_application_unit_id_seq OWNED BY mt_application_unit.application_unit_id;


--
-- TOC entry 192 (class 1259 OID 19014)
-- Name: mt_business_scope; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_business_scope (
    business_scope_id integer NOT NULL,
    business_scope_code character varying(10) NOT NULL,
    description character varying(100) NOT NULL
);


--
-- TOC entry 3195 (class 0 OID 0)
-- Dependencies: 192
-- Name: TABLE mt_business_scope; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_business_scope IS 'Master table of the different types of business scope';


--
-- TOC entry 3196 (class 0 OID 0)
-- Dependencies: 192
-- Name: COLUMN mt_business_scope.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_business_scope.business_scope_id IS 'Internal identifier of the type of business scope';


--
-- TOC entry 3197 (class 0 OID 0)
-- Dependencies: 192
-- Name: COLUMN mt_business_scope.business_scope_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_business_scope.business_scope_code IS 'Business scope code: CORP-corporate; PERS-Personal; IND-indifferent';


--
-- TOC entry 3198 (class 0 OID 0)
-- Dependencies: 192
-- Name: COLUMN mt_business_scope.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_business_scope.description IS 'Description of the type of business scope';


--
-- TOC entry 193 (class 1259 OID 19017)
-- Name: mt_business_scope_business_scope_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE mt_business_scope_business_scope_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3200 (class 0 OID 0)
-- Dependencies: 193
-- Name: mt_business_scope_business_scope_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE mt_business_scope_business_scope_id_seq OWNED BY mt_business_scope.business_scope_id;


--
-- TOC entry 194 (class 1259 OID 19019)
-- Name: mt_consumption_code; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_consumption_code (
    consumption_code_id integer NOT NULL,
    consumption_code character varying(10) NOT NULL,
    description character varying(10) NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    consumption_type_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3202 (class 0 OID 0)
-- Dependencies: 194
-- Name: TABLE mt_consumption_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_consumption_code IS 'Master table of codes of consumption';


--
-- TOC entry 3203 (class 0 OID 0)
-- Dependencies: 194
-- Name: COLUMN mt_consumption_code.consumption_code_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_code.consumption_code_id IS 'Internal identifier of the consumption code';


--
-- TOC entry 3204 (class 0 OID 0)
-- Dependencies: 194
-- Name: COLUMN mt_consumption_code.consumption_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_code.consumption_code IS 'Consumption code';


--
-- TOC entry 3205 (class 0 OID 0)
-- Dependencies: 194
-- Name: COLUMN mt_consumption_code.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_code.description IS 'Description of the consumption code';


--
-- TOC entry 3206 (class 0 OID 0)
-- Dependencies: 194
-- Name: COLUMN mt_consumption_code.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_code.start_date IS 'Start date of the current record of the consumption code (historic changes)';


--
-- TOC entry 3207 (class 0 OID 0)
-- Dependencies: 194
-- Name: COLUMN mt_consumption_code.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_code.end_date IS 'End date of the current record of the consumption code (historic changes)';


--
-- TOC entry 3208 (class 0 OID 0)
-- Dependencies: 194
-- Name: COLUMN mt_consumption_code.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_code.status_id IS 'Status id of the current consumption code';


--
-- TOC entry 3209 (class 0 OID 0)
-- Dependencies: 194
-- Name: COLUMN mt_consumption_code.consumption_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_code.consumption_type_id IS 'Consumption type (fixed phone call, mobile, ppv, sms, ...) id associated to the consumption code';


--
-- TOC entry 3210 (class 0 OID 0)
-- Dependencies: 194
-- Name: COLUMN mt_consumption_code.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_code.business_scope_id IS ' Business scope (corporate, personal, ...) id associated to the consumption code';


--
-- TOC entry 3211 (class 0 OID 0)
-- Dependencies: 194
-- Name: COLUMN mt_consumption_code.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_code.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the consumption code';


--
-- TOC entry 3212 (class 0 OID 0)
-- Dependencies: 194
-- Name: COLUMN mt_consumption_code.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_code.input_date IS 'Date on which the record was created';


--
-- TOC entry 3213 (class 0 OID 0)
-- Dependencies: 194
-- Name: COLUMN mt_consumption_code.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_code.input_user IS 'User who created the record';


--
-- TOC entry 3214 (class 0 OID 0)
-- Dependencies: 194
-- Name: COLUMN mt_consumption_code.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_code.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3215 (class 0 OID 0)
-- Dependencies: 194
-- Name: COLUMN mt_consumption_code.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_code.modif_user IS 'User who done the last modified';


--
-- TOC entry 195 (class 1259 OID 19025)
-- Name: mt_consumption_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_consumption_type (
    consumption_type_id integer NOT NULL,
    consumption_type_code character varying(10) NOT NULL,
    description character varying(100) NOT NULL
);


--
-- TOC entry 3217 (class 0 OID 0)
-- Dependencies: 195
-- Name: TABLE mt_consumption_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_consumption_type IS 'Master table of consumption types (fixed phone calls, mobile calls, ppv, sms, mms, MB';


--
-- TOC entry 3218 (class 0 OID 0)
-- Dependencies: 195
-- Name: COLUMN mt_consumption_type.consumption_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_type.consumption_type_id IS 'Internal identifier of consumption type';


--
-- TOC entry 3219 (class 0 OID 0)
-- Dependencies: 195
-- Name: COLUMN mt_consumption_type.consumption_type_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_type.consumption_type_code IS 'Consumption type code: FCAL-fixed phone call; MCAL-mobile call; PPV: pay per view; SMS: sms; MMS: mms; MB: mega bytes';


--
-- TOC entry 3220 (class 0 OID 0)
-- Dependencies: 195
-- Name: COLUMN mt_consumption_type.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_consumption_type.description IS 'Description of consumption type';


--
-- TOC entry 196 (class 1259 OID 19028)
-- Name: mt_consumption_type_consumption_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE mt_consumption_type_consumption_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3222 (class 0 OID 0)
-- Dependencies: 196
-- Name: mt_consumption_type_consumption_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE mt_consumption_type_consumption_type_id_seq OWNED BY mt_consumption_type.consumption_type_id;


--
-- TOC entry 197 (class 1259 OID 19030)
-- Name: mt_discount_concept; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_discount_concept (
    discount_concept_id integer NOT NULL,
    discount_concept_code character varying(10) NOT NULL,
    description character varying(100) NOT NULL
);


--
-- TOC entry 3224 (class 0 OID 0)
-- Dependencies: 197
-- Name: TABLE mt_discount_concept; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_discount_concept IS 'Master table of discount concepts (fixed phone calls, mobile calls, ppv, fees, ...) ';


--
-- TOC entry 3225 (class 0 OID 0)
-- Dependencies: 197
-- Name: COLUMN mt_discount_concept.discount_concept_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_discount_concept.discount_concept_id IS 'Internal identifier of discount concept';


--
-- TOC entry 3226 (class 0 OID 0)
-- Dependencies: 197
-- Name: COLUMN mt_discount_concept.discount_concept_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_discount_concept.discount_concept_code IS 'Discount concept code: FEE-fee; FPH-fixed phone; MOB-mobile; PPV-ppv)';


--
-- TOC entry 3227 (class 0 OID 0)
-- Dependencies: 197
-- Name: COLUMN mt_discount_concept.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_discount_concept.description IS 'Description of discount concept';


--
-- TOC entry 198 (class 1259 OID 19033)
-- Name: mt_discount_concept_discount_concept_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE mt_discount_concept_discount_concept_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3229 (class 0 OID 0)
-- Dependencies: 198
-- Name: mt_discount_concept_discount_concept_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE mt_discount_concept_discount_concept_id_seq OWNED BY mt_discount_concept.discount_concept_id;


--
-- TOC entry 199 (class 1259 OID 19035)
-- Name: mt_discount_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_discount_type (
    discount_type_id integer NOT NULL,
    discount_type_code character varying(10) NOT NULL,
    description character varying(100) NOT NULL
);


--
-- TOC entry 3231 (class 0 OID 0)
-- Dependencies: 199
-- Name: TABLE mt_discount_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_discount_type IS 'Master table of discount types to apply (fixed, variable percentaje, variable amount, ...)';


--
-- TOC entry 3232 (class 0 OID 0)
-- Dependencies: 199
-- Name: COLUMN mt_discount_type.discount_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_discount_type.discount_type_id IS 'Internal indentifier to discount type';


--
-- TOC entry 3233 (class 0 OID 0)
-- Dependencies: 199
-- Name: COLUMN mt_discount_type.discount_type_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_discount_type.discount_type_code IS 'Discount type: FIXED-fixed;VARPER-variable percentaje;VARAMO-variable amount; VARMIN-variable minutes';


--
-- TOC entry 3234 (class 0 OID 0)
-- Dependencies: 199
-- Name: COLUMN mt_discount_type.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_discount_type.description IS 'Description of discount type';


--
-- TOC entry 200 (class 1259 OID 19038)
-- Name: mt_discount_type_discount_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE mt_discount_type_discount_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3236 (class 0 OID 0)
-- Dependencies: 200
-- Name: mt_discount_type_discount_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE mt_discount_type_discount_type_id_seq OWNED BY mt_discount_type.discount_type_id;


--
-- TOC entry 201 (class 1259 OID 19040)
-- Name: mt_entity_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_entity_type (
    entity_type_id integer NOT NULL,
    entity_type_code character varying(10),
    description character varying(100)
);


--
-- TOC entry 3238 (class 0 OID 0)
-- Dependencies: 201
-- Name: TABLE mt_entity_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_entity_type IS 'Master table of the different codes of existing entities';


--
-- TOC entry 3239 (class 0 OID 0)
-- Dependencies: 201
-- Name: COLUMN mt_entity_type.entity_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_entity_type.entity_type_id IS 'Internal identifier of entity code';


--
-- TOC entry 3240 (class 0 OID 0)
-- Dependencies: 201
-- Name: COLUMN mt_entity_type.entity_type_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_entity_type.entity_type_code IS 'Entity code: PROD-product; SERV-service; EQUI-equipment;PROM-promotion;FEE-fee;CONS-consumtion';


--
-- TOC entry 3241 (class 0 OID 0)
-- Dependencies: 201
-- Name: COLUMN mt_entity_type.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_entity_type.description IS 'Description of entity code';


--
-- TOC entry 202 (class 1259 OID 19043)
-- Name: mt_entity_type_entity_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE mt_entity_type_entity_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3243 (class 0 OID 0)
-- Dependencies: 202
-- Name: mt_entity_type_entity_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE mt_entity_type_entity_type_id_seq OWNED BY mt_entity_type.entity_type_id;


--
-- TOC entry 203 (class 1259 OID 19045)
-- Name: mt_equipment_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_equipment_type (
    equipment_type_id integer NOT NULL,
    equipment_code character varying(10) NOT NULL,
    description character varying(100) NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3245 (class 0 OID 0)
-- Dependencies: 203
-- Name: TABLE mt_equipment_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_equipment_type IS 'Master table of equipment types';


--
-- TOC entry 3246 (class 0 OID 0)
-- Dependencies: 203
-- Name: COLUMN mt_equipment_type.equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_equipment_type.equipment_type_id IS 'Internal identifier of the equipment type';


--
-- TOC entry 3247 (class 0 OID 0)
-- Dependencies: 203
-- Name: COLUMN mt_equipment_type.equipment_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_equipment_type.equipment_code IS 'Code of the equipment type';


--
-- TOC entry 3248 (class 0 OID 0)
-- Dependencies: 203
-- Name: COLUMN mt_equipment_type.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_equipment_type.description IS 'Description of the equipment type';


--
-- TOC entry 3249 (class 0 OID 0)
-- Dependencies: 203
-- Name: COLUMN mt_equipment_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_equipment_type.start_date IS 'Start date of the current record of the equipment type (historic changes)';


--
-- TOC entry 3250 (class 0 OID 0)
-- Dependencies: 203
-- Name: COLUMN mt_equipment_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_equipment_type.end_date IS 'End date of the current record of the equipment type (historic changes)';


--
-- TOC entry 3251 (class 0 OID 0)
-- Dependencies: 203
-- Name: COLUMN mt_equipment_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_equipment_type.status_id IS 'Status id of the current equipment type';


--
-- TOC entry 3252 (class 0 OID 0)
-- Dependencies: 203
-- Name: COLUMN mt_equipment_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_equipment_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to the equipment type';


--
-- TOC entry 3253 (class 0 OID 0)
-- Dependencies: 203
-- Name: COLUMN mt_equipment_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_equipment_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the equipment type';


--
-- TOC entry 3254 (class 0 OID 0)
-- Dependencies: 203
-- Name: COLUMN mt_equipment_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_equipment_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3255 (class 0 OID 0)
-- Dependencies: 203
-- Name: COLUMN mt_equipment_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_equipment_type.input_user IS 'User who created the record';


--
-- TOC entry 3256 (class 0 OID 0)
-- Dependencies: 203
-- Name: COLUMN mt_equipment_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_equipment_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3257 (class 0 OID 0)
-- Dependencies: 203
-- Name: COLUMN mt_equipment_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_equipment_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 204 (class 1259 OID 19051)
-- Name: mt_fee_code; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_fee_code (
    fee_code_id integer NOT NULL,
    fee_code character varying(10) NOT NULL,
    description character varying(100) NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    price numeric(11,6) NOT NULL,
    overwrite boolean DEFAULT false,
    application_level_id integer NOT NULL,
    recurrence_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10),
    CONSTRAINT pk_mt_fee_code_price CHECK (((price >= 0.0) AND (price <= 99999.999999)))
);


--
-- TOC entry 3259 (class 0 OID 0)
-- Dependencies: 204
-- Name: TABLE mt_fee_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_fee_code IS 'Master table of code fees';


--
-- TOC entry 3260 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.fee_code_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.fee_code_id IS 'Internal identifier of the fee code';


--
-- TOC entry 3261 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.fee_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.fee_code IS 'Fee code';


--
-- TOC entry 3262 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.description IS 'Description of fee code';


--
-- TOC entry 3263 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.start_date IS 'Start date of the current record of the fee code (historic changes)';


--
-- TOC entry 3264 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.end_date IS 'End date of the current record of the fee code (historic changes)';


--
-- TOC entry 3265 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.status_id IS 'Status id of the current fee code';


--
-- TOC entry 3266 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.price; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.price IS 'Price ofthe current fee code (range: [0,9999,999999])';


--
-- TOC entry 3267 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.overwrite; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.overwrite IS 'It indicates whether the price could be changed by an user into a instance (default value: f)';


--
-- TOC entry 3268 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.application_level_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.application_level_id IS 'Application level id of the current fee code';


--
-- TOC entry 3269 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.recurrence_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.recurrence_id IS 'Recurrence (one off, activation, monthly, biannual, annual,) id of the current fee code';


--
-- TOC entry 3270 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to the fee code';


--
-- TOC entry 3271 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the fee code';


--
-- TOC entry 3272 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.input_date IS 'Date on which the record was created';


--
-- TOC entry 3273 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.input_user IS 'User who created the record';


--
-- TOC entry 3274 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3275 (class 0 OID 0)
-- Dependencies: 204
-- Name: COLUMN mt_fee_code.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_fee_code.modif_user IS 'User who done the last modified';


--
-- TOC entry 205 (class 1259 OID 19059)
-- Name: mt_plan_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_plan_type (
    plan_type_id integer NOT NULL,
    plan_code character varying(10) NOT NULL,
    description character varying(100) NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    application_level_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3277 (class 0 OID 0)
-- Dependencies: 205
-- Name: TABLE mt_plan_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_plan_type IS 'Master table of plan types';


--
-- TOC entry 3278 (class 0 OID 0)
-- Dependencies: 205
-- Name: COLUMN mt_plan_type.plan_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_plan_type.plan_type_id IS 'Internal identifier of the plan type';


--
-- TOC entry 3279 (class 0 OID 0)
-- Dependencies: 205
-- Name: COLUMN mt_plan_type.plan_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_plan_type.plan_code IS 'Code of the plan type';


--
-- TOC entry 3280 (class 0 OID 0)
-- Dependencies: 205
-- Name: COLUMN mt_plan_type.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_plan_type.description IS 'Description of the plan type';


--
-- TOC entry 3281 (class 0 OID 0)
-- Dependencies: 205
-- Name: COLUMN mt_plan_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_plan_type.start_date IS 'Start date of the current record of the plan type (historic changes)';


--
-- TOC entry 3282 (class 0 OID 0)
-- Dependencies: 205
-- Name: COLUMN mt_plan_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_plan_type.end_date IS 'End date of the current record of the plan type (historic changes)';


--
-- TOC entry 3283 (class 0 OID 0)
-- Dependencies: 205
-- Name: COLUMN mt_plan_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_plan_type.status_id IS 'Status id of the current plan type';


--
-- TOC entry 3284 (class 0 OID 0)
-- Dependencies: 205
-- Name: COLUMN mt_plan_type.application_level_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_plan_type.application_level_id IS 'Application level id of the current plan type';


--
-- TOC entry 3285 (class 0 OID 0)
-- Dependencies: 205
-- Name: COLUMN mt_plan_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_plan_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to the plan type';


--
-- TOC entry 3286 (class 0 OID 0)
-- Dependencies: 205
-- Name: COLUMN mt_plan_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_plan_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the plan type';


--
-- TOC entry 3287 (class 0 OID 0)
-- Dependencies: 205
-- Name: COLUMN mt_plan_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_plan_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3288 (class 0 OID 0)
-- Dependencies: 205
-- Name: COLUMN mt_plan_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_plan_type.input_user IS 'User who created the record';


--
-- TOC entry 3289 (class 0 OID 0)
-- Dependencies: 205
-- Name: COLUMN mt_plan_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_plan_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3290 (class 0 OID 0)
-- Dependencies: 205
-- Name: COLUMN mt_plan_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_plan_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 206 (class 1259 OID 19065)
-- Name: mt_product_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_product_type (
    product_type_id integer NOT NULL,
    product_type_code character varying(10) NOT NULL,
    description character varying(100) NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('31/12/9999 23:59:59'::text, 'DD/MM/YYYY HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10),
    entity_type_id integer DEFAULT 2 NOT NULL
);


--
-- TOC entry 3292 (class 0 OID 0)
-- Dependencies: 206
-- Name: TABLE mt_product_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_product_type IS 'Master table of product types';


--
-- TOC entry 3293 (class 0 OID 0)
-- Dependencies: 206
-- Name: COLUMN mt_product_type.product_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_product_type.product_type_id IS 'Internal identifier of the product type';


--
-- TOC entry 3294 (class 0 OID 0)
-- Dependencies: 206
-- Name: COLUMN mt_product_type.product_type_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_product_type.product_type_code IS 'Code of the product type';


--
-- TOC entry 3295 (class 0 OID 0)
-- Dependencies: 206
-- Name: COLUMN mt_product_type.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_product_type.description IS 'Description of the product type';


--
-- TOC entry 3296 (class 0 OID 0)
-- Dependencies: 206
-- Name: COLUMN mt_product_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_product_type.start_date IS 'Start date of the current record of the product type (historic changes)';


--
-- TOC entry 3297 (class 0 OID 0)
-- Dependencies: 206
-- Name: COLUMN mt_product_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_product_type.end_date IS 'End date of the current record of the product type (historic changes)';


--
-- TOC entry 3298 (class 0 OID 0)
-- Dependencies: 206
-- Name: COLUMN mt_product_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_product_type.status_id IS 'Status id of the current product type';


--
-- TOC entry 3299 (class 0 OID 0)
-- Dependencies: 206
-- Name: COLUMN mt_product_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_product_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to the product type';


--
-- TOC entry 3300 (class 0 OID 0)
-- Dependencies: 206
-- Name: COLUMN mt_product_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_product_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the product type';


--
-- TOC entry 3301 (class 0 OID 0)
-- Dependencies: 206
-- Name: COLUMN mt_product_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_product_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3302 (class 0 OID 0)
-- Dependencies: 206
-- Name: COLUMN mt_product_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_product_type.input_user IS 'User who created the record';


--
-- TOC entry 3303 (class 0 OID 0)
-- Dependencies: 206
-- Name: COLUMN mt_product_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_product_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3304 (class 0 OID 0)
-- Dependencies: 206
-- Name: COLUMN mt_product_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_product_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 3305 (class 0 OID 0)
-- Dependencies: 206
-- Name: COLUMN mt_product_type.entity_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_product_type.entity_type_id IS 'Entity type id of the product_type (value: 2)';


--
-- TOC entry 207 (class 1259 OID 19072)
-- Name: mt_profile; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_profile (
    profile_id integer NOT NULL,
    profile_code character varying(10) NOT NULL,
    description character varying(100) NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3307 (class 0 OID 0)
-- Dependencies: 207
-- Name: TABLE mt_profile; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_profile IS 'Master table of profiles application access';


--
-- TOC entry 3308 (class 0 OID 0)
-- Dependencies: 207
-- Name: COLUMN mt_profile.profile_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_profile.profile_id IS 'Internal identifier of the profile';


--
-- TOC entry 3309 (class 0 OID 0)
-- Dependencies: 207
-- Name: COLUMN mt_profile.profile_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_profile.profile_code IS 'Profile code';


--
-- TOC entry 3310 (class 0 OID 0)
-- Dependencies: 207
-- Name: COLUMN mt_profile.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_profile.description IS 'Description of profile';


--
-- TOC entry 3311 (class 0 OID 0)
-- Dependencies: 207
-- Name: COLUMN mt_profile.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_profile.start_date IS 'Start date of the current record of the profile (historic changes)';


--
-- TOC entry 3312 (class 0 OID 0)
-- Dependencies: 207
-- Name: COLUMN mt_profile.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_profile.end_date IS 'End date of the current record of the profile (historic changes)';


--
-- TOC entry 3313 (class 0 OID 0)
-- Dependencies: 207
-- Name: COLUMN mt_profile.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_profile.input_date IS 'Date on which the record was created';


--
-- TOC entry 3314 (class 0 OID 0)
-- Dependencies: 207
-- Name: COLUMN mt_profile.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_profile.input_user IS 'User who created the record';


--
-- TOC entry 3315 (class 0 OID 0)
-- Dependencies: 207
-- Name: COLUMN mt_profile.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_profile.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3316 (class 0 OID 0)
-- Dependencies: 207
-- Name: COLUMN mt_profile.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_profile.modif_user IS 'User who done the last modified';


--
-- TOC entry 208 (class 1259 OID 19078)
-- Name: mt_promotion_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_promotion_type (
    promotion_type_id integer NOT NULL,
    promotion_code character varying(10) NOT NULL,
    voucher boolean DEFAULT false NOT NULL,
    description character varying(100) NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    application_level_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3318 (class 0 OID 0)
-- Dependencies: 208
-- Name: TABLE mt_promotion_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_promotion_type IS 'Master table of promotion types';


--
-- TOC entry 3319 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN mt_promotion_type.promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_promotion_type.promotion_type_id IS 'Internal identifier of the promotion type';


--
-- TOC entry 3320 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN mt_promotion_type.promotion_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_promotion_type.promotion_code IS 'Code of the promotion type';


--
-- TOC entry 3321 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN mt_promotion_type.voucher; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_promotion_type.voucher IS 'It indicates whether the promotion is a voucher (default value: false)';


--
-- TOC entry 3322 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN mt_promotion_type.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_promotion_type.description IS 'Description of the promotion type';


--
-- TOC entry 3323 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN mt_promotion_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_promotion_type.start_date IS 'Start date of the current record of the promotion type (historic changes)';


--
-- TOC entry 3324 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN mt_promotion_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_promotion_type.end_date IS 'End date of the current record of the promotion type (historic changes)';


--
-- TOC entry 3325 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN mt_promotion_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_promotion_type.status_id IS 'Status id of the current promotion type';


--
-- TOC entry 3326 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN mt_promotion_type.application_level_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_promotion_type.application_level_id IS 'Application level id of the current promotion type';


--
-- TOC entry 3327 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN mt_promotion_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_promotion_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to the promotion type';


--
-- TOC entry 3328 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN mt_promotion_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_promotion_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the promotion type';


--
-- TOC entry 3329 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN mt_promotion_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_promotion_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3330 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN mt_promotion_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_promotion_type.input_user IS 'User who created the record';


--
-- TOC entry 3331 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN mt_promotion_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_promotion_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3332 (class 0 OID 0)
-- Dependencies: 208
-- Name: COLUMN mt_promotion_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_promotion_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 209 (class 1259 OID 19085)
-- Name: mt_recurrence; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_recurrence (
    recurrence_id integer NOT NULL,
    recurrence_code character varying(10),
    description character varying(100)
);


--
-- TOC entry 3334 (class 0 OID 0)
-- Dependencies: 209
-- Name: TABLE mt_recurrence; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_recurrence IS 'Master table of recurrence types';


--
-- TOC entry 3335 (class 0 OID 0)
-- Dependencies: 209
-- Name: COLUMN mt_recurrence.recurrence_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_recurrence.recurrence_id IS 'Internal identifier of recurrence type';


--
-- TOC entry 3336 (class 0 OID 0)
-- Dependencies: 209
-- Name: COLUMN mt_recurrence.recurrence_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_recurrence.recurrence_code IS 'Recurrence code: ONEOFF-one off; INPUT-input; MONTH-monthly; BIMONT-bimonthly; BIANNU-bianual(sixt months); ANNUAL-annual';


--
-- TOC entry 3337 (class 0 OID 0)
-- Dependencies: 209
-- Name: COLUMN mt_recurrence.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_recurrence.description IS 'Description of recurrence';


--
-- TOC entry 210 (class 1259 OID 19088)
-- Name: mt_recurrence_recurrence_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE mt_recurrence_recurrence_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3339 (class 0 OID 0)
-- Dependencies: 210
-- Name: mt_recurrence_recurrence_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE mt_recurrence_recurrence_id_seq OWNED BY mt_recurrence.recurrence_id;


--
-- TOC entry 211 (class 1259 OID 19090)
-- Name: mt_service_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_service_type (
    service_type_id integer NOT NULL,
    service_type_code character varying(10) NOT NULL,
    description character varying(100) NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('31/12/9999 23:59:59'::text, 'DD/MM/YYYY HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10),
    entity_type_id integer DEFAULT 3 NOT NULL
);


--
-- TOC entry 3341 (class 0 OID 0)
-- Dependencies: 211
-- Name: TABLE mt_service_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_service_type IS 'Master table of service types';


--
-- TOC entry 3342 (class 0 OID 0)
-- Dependencies: 211
-- Name: COLUMN mt_service_type.service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_service_type.service_type_id IS 'Internal identifier of the service type';


--
-- TOC entry 3343 (class 0 OID 0)
-- Dependencies: 211
-- Name: COLUMN mt_service_type.service_type_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_service_type.service_type_code IS 'Code of the service type';


--
-- TOC entry 3344 (class 0 OID 0)
-- Dependencies: 211
-- Name: COLUMN mt_service_type.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_service_type.description IS 'Description of the service type';


--
-- TOC entry 3345 (class 0 OID 0)
-- Dependencies: 211
-- Name: COLUMN mt_service_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_service_type.start_date IS 'Start date of the current record of the service type (historic changes)';


--
-- TOC entry 3346 (class 0 OID 0)
-- Dependencies: 211
-- Name: COLUMN mt_service_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_service_type.end_date IS 'End date of the current record of the service type (historic changes)';


--
-- TOC entry 3347 (class 0 OID 0)
-- Dependencies: 211
-- Name: COLUMN mt_service_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_service_type.status_id IS 'Status id of the current service type';


--
-- TOC entry 3348 (class 0 OID 0)
-- Dependencies: 211
-- Name: COLUMN mt_service_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_service_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to the service type';


--
-- TOC entry 3349 (class 0 OID 0)
-- Dependencies: 211
-- Name: COLUMN mt_service_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_service_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the service type';


--
-- TOC entry 3350 (class 0 OID 0)
-- Dependencies: 211
-- Name: COLUMN mt_service_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_service_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3351 (class 0 OID 0)
-- Dependencies: 211
-- Name: COLUMN mt_service_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_service_type.input_user IS 'User who created the record';


--
-- TOC entry 3352 (class 0 OID 0)
-- Dependencies: 211
-- Name: COLUMN mt_service_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_service_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3353 (class 0 OID 0)
-- Dependencies: 211
-- Name: COLUMN mt_service_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_service_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 3354 (class 0 OID 0)
-- Dependencies: 211
-- Name: COLUMN mt_service_type.entity_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_service_type.entity_type_id IS 'Entity type id of the service_type (value: 3)';


--
-- TOC entry 212 (class 1259 OID 19097)
-- Name: mt_status; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_status (
    status_id integer NOT NULL,
    entity_type_id integer NOT NULL,
    status_code character varying(10),
    description character varying(100)
);


--
-- TOC entry 3356 (class 0 OID 0)
-- Dependencies: 212
-- Name: TABLE mt_status; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_status IS 'Master table of different types of existing states for an entity';


--
-- TOC entry 3357 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN mt_status.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_status.status_id IS 'Internal identifier of status';


--
-- TOC entry 3358 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN mt_status.entity_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_status.entity_type_id IS 'Type of entity that is associated with the given state';


--
-- TOC entry 3359 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN mt_status.status_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_status.status_code IS 'Status code';


--
-- TOC entry 3360 (class 0 OID 0)
-- Dependencies: 212
-- Name: COLUMN mt_status.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_status.description IS 'Description of the status';


--
-- TOC entry 213 (class 1259 OID 19100)
-- Name: mt_status_status_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE mt_status_status_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3362 (class 0 OID 0)
-- Dependencies: 213
-- Name: mt_status_status_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE mt_status_status_id_seq OWNED BY mt_status.status_id;


--
-- TOC entry 214 (class 1259 OID 19102)
-- Name: mt_tariff_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_tariff_type (
    tariff_type_id integer NOT NULL,
    tariff_code character varying(10) NOT NULL,
    prorrate boolean DEFAULT false NOT NULL,
    description character varying(100) NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    application_level_id integer NOT NULL,
    recurrence_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3364 (class 0 OID 0)
-- Dependencies: 214
-- Name: TABLE mt_tariff_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_tariff_type IS 'Master table of tariff types';


--
-- TOC entry 3365 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN mt_tariff_type.tariff_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_tariff_type.tariff_type_id IS 'Internal identifier of the tariff type';


--
-- TOC entry 3366 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN mt_tariff_type.tariff_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_tariff_type.tariff_code IS 'Code of the tariff type';


--
-- TOC entry 3367 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN mt_tariff_type.prorrate; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_tariff_type.prorrate IS 'It indicates whether the tariff is prorrated (default value: false)';


--
-- TOC entry 3368 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN mt_tariff_type.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_tariff_type.description IS 'Description of the tariff type';


--
-- TOC entry 3369 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN mt_tariff_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_tariff_type.start_date IS 'Start date of the current record of the tariff type (historic changes)';


--
-- TOC entry 3370 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN mt_tariff_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_tariff_type.end_date IS 'End date of the current record of the tariff type (historic changes)';


--
-- TOC entry 3371 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN mt_tariff_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_tariff_type.status_id IS 'Status id of the current tariff type';


--
-- TOC entry 3372 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN mt_tariff_type.application_level_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_tariff_type.application_level_id IS 'Application level id of the current tariff type';


--
-- TOC entry 3373 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN mt_tariff_type.recurrence_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_tariff_type.recurrence_id IS 'Recurrence (one off, activation, monthly, biannual, annual,) id of the current tariff type';


--
-- TOC entry 3374 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN mt_tariff_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_tariff_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to the tariff type';


--
-- TOC entry 3375 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN mt_tariff_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_tariff_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the tariff type';


--
-- TOC entry 3376 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN mt_tariff_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_tariff_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3377 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN mt_tariff_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_tariff_type.input_user IS 'User who created the record';


--
-- TOC entry 3378 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN mt_tariff_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_tariff_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3379 (class 0 OID 0)
-- Dependencies: 214
-- Name: COLUMN mt_tariff_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_tariff_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 215 (class 1259 OID 19109)
-- Name: mt_technology_scope; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE mt_technology_scope (
    technology_scope_id integer NOT NULL,
    technology_scope_code character varying(10) NOT NULL,
    description character varying(100) NOT NULL
);


--
-- TOC entry 3381 (class 0 OID 0)
-- Dependencies: 215
-- Name: TABLE mt_technology_scope; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE mt_technology_scope IS 'Master table of technology scope (Fixed phone, mobil, television...)';


--
-- TOC entry 3382 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN mt_technology_scope.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_technology_scope.technology_scope_id IS 'Internal identifier of technology scope';


--
-- TOC entry 3383 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN mt_technology_scope.technology_scope_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_technology_scope.technology_scope_code IS 'Technology scope code: FIX-fixed phone; MOB-mobil; BTV-basic television; ...';


--
-- TOC entry 3384 (class 0 OID 0)
-- Dependencies: 215
-- Name: COLUMN mt_technology_scope.description; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN mt_technology_scope.description IS 'Description of technology scope';


--
-- TOC entry 216 (class 1259 OID 19112)
-- Name: mt_technology_scope_technology_scope_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE mt_technology_scope_technology_scope_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3386 (class 0 OID 0)
-- Dependencies: 216
-- Name: mt_technology_scope_technology_scope_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE mt_technology_scope_technology_scope_id_seq OWNED BY mt_technology_scope.technology_scope_id;


--
-- TOC entry 217 (class 1259 OID 19114)
-- Name: rmt_fee_equip_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_fee_equip_type (
    fee_equipment_type_id integer NOT NULL,
    fee_code_id integer NOT NULL,
    equipment_type_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3388 (class 0 OID 0)
-- Dependencies: 217
-- Name: TABLE rmt_fee_equip_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_fee_equip_type IS 'Relational master table of fee codes and equipment types. It provides the relationship between a code of fee and the types of equipment on which it can apply';


--
-- TOC entry 3389 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN rmt_fee_equip_type.fee_equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_equip_type.fee_equipment_type_id IS 'Internal identifier of the relationship between fee and equipment types';


--
-- TOC entry 3390 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN rmt_fee_equip_type.fee_code_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_equip_type.fee_code_id IS 'Fee code id';


--
-- TOC entry 3391 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN rmt_fee_equip_type.equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_equip_type.equipment_type_id IS 'Equipment type id';


--
-- TOC entry 3392 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN rmt_fee_equip_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_equip_type.start_date IS 'Start date of the current record of the fee_equip_type (historic changes)';


--
-- TOC entry 3393 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN rmt_fee_equip_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_equip_type.end_date IS 'End date of the current record of the fee_equip_type (historic changes)';


--
-- TOC entry 3394 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN rmt_fee_equip_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_equip_type.status_id IS 'Status id of the current fee_equip_type';


--
-- TOC entry 3395 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN rmt_fee_equip_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_equip_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to fee_equip_type';


--
-- TOC entry 3396 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN rmt_fee_equip_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_equip_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the fee_equip_type';


--
-- TOC entry 3397 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN rmt_fee_equip_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_equip_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3398 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN rmt_fee_equip_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_equip_type.input_user IS 'User who created the record';


--
-- TOC entry 3399 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN rmt_fee_equip_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_equip_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3400 (class 0 OID 0)
-- Dependencies: 217
-- Name: COLUMN rmt_fee_equip_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_equip_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 218 (class 1259 OID 19120)
-- Name: rmt_fee_product_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_fee_product_type (
    fee_product_type_id integer NOT NULL,
    fee_code_id integer NOT NULL,
    product_type_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3402 (class 0 OID 0)
-- Dependencies: 218
-- Name: TABLE rmt_fee_product_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_fee_product_type IS 'Relational master table of fee and product types. It provides the relationship between a type of fee and the types of product on which it can apply';


--
-- TOC entry 3403 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN rmt_fee_product_type.fee_product_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_product_type.fee_product_type_id IS 'Internal identifier of the relationship between fee and product types';


--
-- TOC entry 3404 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN rmt_fee_product_type.fee_code_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_product_type.fee_code_id IS 'Fee code id';


--
-- TOC entry 3405 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN rmt_fee_product_type.product_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_product_type.product_type_id IS 'Product type id';


--
-- TOC entry 3406 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN rmt_fee_product_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_product_type.start_date IS 'Start date of the current record of the fee_product_type (historic changes)';


--
-- TOC entry 3407 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN rmt_fee_product_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_product_type.end_date IS 'End date of the current record of the fee_product_type (historic changes)';


--
-- TOC entry 3408 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN rmt_fee_product_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_product_type.status_id IS 'Status id of the current fee_product_type';


--
-- TOC entry 3409 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN rmt_fee_product_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_product_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to fee_product_type';


--
-- TOC entry 3410 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN rmt_fee_product_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_product_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the fee_product_type';


--
-- TOC entry 3411 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN rmt_fee_product_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_product_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3412 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN rmt_fee_product_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_product_type.input_user IS 'User who created the record';


--
-- TOC entry 3413 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN rmt_fee_product_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_product_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3414 (class 0 OID 0)
-- Dependencies: 218
-- Name: COLUMN rmt_fee_product_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_product_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 219 (class 1259 OID 19126)
-- Name: rmt_fee_promotion_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_fee_promotion_type (
    fee_promotion_type_id integer NOT NULL,
    fee_code_id integer NOT NULL,
    promotion_type_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3416 (class 0 OID 0)
-- Dependencies: 219
-- Name: TABLE rmt_fee_promotion_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_fee_promotion_type IS 'Relational master table of fee and promotion types. It provides the relationship between a type of fee and the types of promotion on which it can apply';


--
-- TOC entry 3417 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN rmt_fee_promotion_type.fee_promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_promotion_type.fee_promotion_type_id IS 'Internal identifier of the relationship between fee and promotion types';


--
-- TOC entry 3418 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN rmt_fee_promotion_type.fee_code_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_promotion_type.fee_code_id IS 'Fee type id';


--
-- TOC entry 3419 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN rmt_fee_promotion_type.promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_promotion_type.promotion_type_id IS 'Promotion type id';


--
-- TOC entry 3420 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN rmt_fee_promotion_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_promotion_type.start_date IS 'Start date of the current record of the fee_promotion_type (historic changes)';


--
-- TOC entry 3421 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN rmt_fee_promotion_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_promotion_type.end_date IS 'End date of the current record of the fee_promotion_type (historic changes)';


--
-- TOC entry 3422 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN rmt_fee_promotion_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_promotion_type.status_id IS 'Status id of the current fee_promotion_type';


--
-- TOC entry 3423 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN rmt_fee_promotion_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_promotion_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to fee_promotion_type';


--
-- TOC entry 3424 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN rmt_fee_promotion_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_promotion_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the fee_promotion_type';


--
-- TOC entry 3425 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN rmt_fee_promotion_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_promotion_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3426 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN rmt_fee_promotion_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_promotion_type.input_user IS 'User who created the record';


--
-- TOC entry 3427 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN rmt_fee_promotion_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_promotion_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3428 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN rmt_fee_promotion_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_promotion_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 220 (class 1259 OID 19132)
-- Name: rmt_fee_service_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_fee_service_type (
    fee_service_type_id integer NOT NULL,
    fee_code_id integer NOT NULL,
    service_type_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3430 (class 0 OID 0)
-- Dependencies: 220
-- Name: TABLE rmt_fee_service_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_fee_service_type IS 'Relational master table of fee and service types. It provides the relationship between a type of fee and the types of service on which it can apply';


--
-- TOC entry 3431 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN rmt_fee_service_type.fee_service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_service_type.fee_service_type_id IS 'Internal identifier of the relationship between fee and service types';


--
-- TOC entry 3432 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN rmt_fee_service_type.fee_code_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_service_type.fee_code_id IS 'Fee code id';


--
-- TOC entry 3433 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN rmt_fee_service_type.service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_service_type.service_type_id IS 'Service type id';


--
-- TOC entry 3434 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN rmt_fee_service_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_service_type.start_date IS 'Start date of the current record of the fee_service_type (historic changes)';


--
-- TOC entry 3435 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN rmt_fee_service_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_service_type.end_date IS 'End date of the current record of the fee_service_type (historic changes)';


--
-- TOC entry 3436 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN rmt_fee_service_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_service_type.status_id IS 'Status id of the current fee_service_type';


--
-- TOC entry 3437 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN rmt_fee_service_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_service_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to fee_service_type';


--
-- TOC entry 3438 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN rmt_fee_service_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_service_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the fee_service_type';


--
-- TOC entry 3439 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN rmt_fee_service_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_service_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3440 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN rmt_fee_service_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_service_type.input_user IS 'User who created the record';


--
-- TOC entry 3441 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN rmt_fee_service_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_service_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3442 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN rmt_fee_service_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_fee_service_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 221 (class 1259 OID 19138)
-- Name: rmt_plan_charge_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_plan_charge_type (
    plan_charge_type_id integer NOT NULL,
    plan_type_id integer NOT NULL,
    charge_type character varying(5) NOT NULL,
    charge_type_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3444 (class 0 OID 0)
-- Dependencies: 221
-- Name: TABLE rmt_plan_charge_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_plan_charge_type IS 'Relational master table of charge types and plans. It indicates the charges (fee/consumption) on which the plan applies';


--
-- TOC entry 3445 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN rmt_plan_charge_type.plan_charge_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_charge_type.plan_charge_type_id IS 'Internal identifier of the relationship between plan and charge types';


--
-- TOC entry 3446 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN rmt_plan_charge_type.plan_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_charge_type.plan_type_id IS 'Plan id';


--
-- TOC entry 3447 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN rmt_plan_charge_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_charge_type.start_date IS 'Start date of the current record of the plan_charge_type (historic changes)';


--
-- TOC entry 3448 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN rmt_plan_charge_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_charge_type.end_date IS 'End date of the current record of the plan_charge_type (historic changes)';


--
-- TOC entry 3449 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN rmt_plan_charge_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_charge_type.status_id IS 'Status id of the current plan_charge_type';


--
-- TOC entry 3450 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN rmt_plan_charge_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_charge_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3451 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN rmt_plan_charge_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_charge_type.input_user IS 'User who created the record';


--
-- TOC entry 3452 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN rmt_plan_charge_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_charge_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3453 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN rmt_plan_charge_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_charge_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 222 (class 1259 OID 19144)
-- Name: rmt_plan_discount_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_plan_discount_type (
    plan_discount_type_id integer NOT NULL,
    plan_type_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    percentaje numeric(5,2) NOT NULL,
    threshold_unit_id integer,
    min_threshold integer,
    max_threshold integer,
    discount_unit_id integer,
    max_discount numeric(12,6) NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10),
    CONSTRAINT pk_rmt_plan_discount_max_d CHECK (((max_discount >= 0.00) AND (max_discount <= (999999)::numeric))),
    CONSTRAINT pk_rmt_plan_discount_max_t CHECK ((max_threshold <= 999999)),
    CONSTRAINT pk_rmt_plan_discount_min_t CHECK ((min_threshold >= 0)),
    CONSTRAINT pk_rmt_plan_discount_perc CHECK (((percentaje >= 0.00) AND (percentaje <= 100.00))),
    CONSTRAINT pk_rmt_plan_discount_thr CHECK ((min_threshold <= max_threshold))
);


--
-- TOC entry 3455 (class 0 OID 0)
-- Dependencies: 222
-- Name: TABLE rmt_plan_discount_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_plan_discount_type IS 'Relational master table of plans and discount types. It indicates the discount applied by the plan';


--
-- TOC entry 3456 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN rmt_plan_discount_type.plan_discount_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_discount_type.plan_discount_type_id IS 'Internal identifier of the relationship between plan and discount to apply';


--
-- TOC entry 3457 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN rmt_plan_discount_type.plan_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_discount_type.plan_type_id IS 'Plan Id';


--
-- TOC entry 3458 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN rmt_plan_discount_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_discount_type.start_date IS 'Start date of the current record of the plan_discount_type (historic changes)';


--
-- TOC entry 3459 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN rmt_plan_discount_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_discount_type.end_date IS 'End date of the current record of the plan_discount_type (historic changes)';


--
-- TOC entry 3460 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN rmt_plan_discount_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_discount_type.status_id IS 'Status id of the current plan_discount_type';


--
-- TOC entry 3461 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN rmt_plan_discount_type.percentaje; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_discount_type.percentaje IS 'Percentaje of discount to apply';


--
-- TOC entry 3462 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN rmt_plan_discount_type.threshold_unit_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_discount_type.threshold_unit_id IS 'Application unit id for calculating the threshold';


--
-- TOC entry 3463 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN rmt_plan_discount_type.min_threshold; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_discount_type.min_threshold IS 'Minimum range from which the promotion applies';


--
-- TOC entry 3464 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN rmt_plan_discount_type.max_threshold; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_discount_type.max_threshold IS 'Maximum range to which the promotion applies';


--
-- TOC entry 3465 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN rmt_plan_discount_type.discount_unit_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_discount_type.discount_unit_id IS 'Application unit id for calculating the discount';


--
-- TOC entry 3466 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN rmt_plan_discount_type.max_discount; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_discount_type.max_discount IS 'Maximum discount for the discount unit id';


--
-- TOC entry 3467 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN rmt_plan_discount_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_discount_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3468 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN rmt_plan_discount_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_discount_type.input_user IS 'User who created the record';


--
-- TOC entry 3469 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN rmt_plan_discount_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_discount_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3470 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN rmt_plan_discount_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_discount_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 223 (class 1259 OID 19155)
-- Name: rmt_plan_prerreq_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_plan_prerreq_type (
    plan_prerreq_type_id integer NOT NULL,
    plan_type_id integer NOT NULL,
    prerrequisite_type character varying(5) NOT NULL,
    prerrequisite_type_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3472 (class 0 OID 0)
-- Dependencies: 223
-- Name: TABLE rmt_plan_prerreq_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_plan_prerreq_type IS 'Relational master table of prerrequisite types and plans. It indicates the prerequisites (fee/consumption) that must exist (at least one) for the plan can be applied';


--
-- TOC entry 3473 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN rmt_plan_prerreq_type.plan_prerreq_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_prerreq_type.plan_prerreq_type_id IS 'Internal identifier of the relationship between plan and prerrequisite types';


--
-- TOC entry 3474 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN rmt_plan_prerreq_type.plan_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_prerreq_type.plan_type_id IS 'Plan id';


--
-- TOC entry 3475 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN rmt_plan_prerreq_type.prerrequisite_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_prerreq_type.prerrequisite_type IS 'Prerrequisite type: FEE-fee; CONS-consumption';


--
-- TOC entry 3476 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN rmt_plan_prerreq_type.prerrequisite_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_prerreq_type.prerrequisite_type_id IS 'Prerrequisite type id. Depending on the prerrequisite type will be fee_type_id or consumption_type_id';


--
-- TOC entry 3477 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN rmt_plan_prerreq_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_prerreq_type.start_date IS 'Start date of the current record of the plan_prerreq_type (historic changes)';


--
-- TOC entry 3478 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN rmt_plan_prerreq_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_prerreq_type.end_date IS 'End date of the current record of the plan_prerreq_type (historic changes)';


--
-- TOC entry 3479 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN rmt_plan_prerreq_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_prerreq_type.status_id IS 'Status id of the current plan_prerreq_type';


--
-- TOC entry 3480 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN rmt_plan_prerreq_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_prerreq_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3481 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN rmt_plan_prerreq_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_prerreq_type.input_user IS 'User who created the record';


--
-- TOC entry 3482 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN rmt_plan_prerreq_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_prerreq_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3483 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN rmt_plan_prerreq_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_prerreq_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 224 (class 1259 OID 19161)
-- Name: rmt_plan_promotion_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_plan_promotion_type (
    plan_promotion_type_id integer NOT NULL,
    promotion_type_id integer NOT NULL,
    promotion_code character varying(10) NOT NULL,
    plan_type_id integer NOT NULL,
    plan_code character varying(10) NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer,
    discount_type_id integer,
    discount_concept_id integer,
    prorrate boolean DEFAULT false NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3485 (class 0 OID 0)
-- Dependencies: 224
-- Name: TABLE rmt_plan_promotion_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_plan_promotion_type IS 'Relational master table of promotion types and plans. It provides the relationship between the promotion type and the plans that compose it';


--
-- TOC entry 3486 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN rmt_plan_promotion_type.plan_promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_promotion_type.plan_promotion_type_id IS 'Internal identifier of the relationship between promotion type and plans';


--
-- TOC entry 3487 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN rmt_plan_promotion_type.promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_promotion_type.promotion_type_id IS 'Promotion type id';


--
-- TOC entry 3488 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN rmt_plan_promotion_type.promotion_code; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_promotion_type.promotion_code IS 'Code of the promotion type';


--
-- TOC entry 3489 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN rmt_plan_promotion_type.plan_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_promotion_type.plan_type_id IS 'Internal identifier for the plan associated with the promotion type';


--
-- TOC entry 3490 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN rmt_plan_promotion_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_promotion_type.end_date IS 'End date of the current record of the plan_promotion_type (historic changes)';


--
-- TOC entry 3491 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN rmt_plan_promotion_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_promotion_type.status_id IS 'Status id of the current plan_promotion_type';


--
-- TOC entry 3492 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN rmt_plan_promotion_type.discount_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_promotion_type.discount_type_id IS 'Discount type id of the plan_promotion_type';


--
-- TOC entry 3493 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN rmt_plan_promotion_type.discount_concept_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_promotion_type.discount_concept_id IS 'Discount concept id of the plan_promotion_type';


--
-- TOC entry 3494 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN rmt_plan_promotion_type.prorrate; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_promotion_type.prorrate IS 'It indicates whether the plan is prorrated (default value: false)';


--
-- TOC entry 3495 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN rmt_plan_promotion_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_promotion_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3496 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN rmt_plan_promotion_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_promotion_type.input_user IS 'User who created the record';


--
-- TOC entry 3497 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN rmt_plan_promotion_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_promotion_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3498 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN rmt_plan_promotion_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_plan_promotion_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 225 (class 1259 OID 19168)
-- Name: rmt_promotion_equipment_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_promotion_equipment_type (
    prom_equip_type_id integer NOT NULL,
    promotion_type_id integer NOT NULL,
    equipment_type_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3500 (class 0 OID 0)
-- Dependencies: 225
-- Name: TABLE rmt_promotion_equipment_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_promotion_equipment_type IS 'Relational master table of promotion types and equipment types according to the business scope and the technology scope.';


--
-- TOC entry 3501 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN rmt_promotion_equipment_type.promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_equipment_type.promotion_type_id IS 'Promotion type id';


--
-- TOC entry 3502 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN rmt_promotion_equipment_type.equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_equipment_type.equipment_type_id IS 'Equipment type id';


--
-- TOC entry 3503 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN rmt_promotion_equipment_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_equipment_type.start_date IS 'Start date of the current record of the promotion_equipment_type (historic changes)';


--
-- TOC entry 3504 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN rmt_promotion_equipment_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_equipment_type.end_date IS 'End date of the current record of the promotion_equipment_type (historic changes)';


--
-- TOC entry 3505 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN rmt_promotion_equipment_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_equipment_type.status_id IS 'Status id of the current promotion_equipment_type';


--
-- TOC entry 3506 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN rmt_promotion_equipment_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_equipment_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to the promotion_equipment_type';


--
-- TOC entry 3507 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN rmt_promotion_equipment_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_equipment_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the promotion_equipment_type';


--
-- TOC entry 3508 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN rmt_promotion_equipment_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_equipment_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3509 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN rmt_promotion_equipment_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_equipment_type.input_user IS 'User who created the record';


--
-- TOC entry 3510 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN rmt_promotion_equipment_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_equipment_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3511 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN rmt_promotion_equipment_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_equipment_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 226 (class 1259 OID 19174)
-- Name: rmt_promotion_product_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_promotion_product_type (
    prom_prod_type_id integer NOT NULL,
    promotion_type_id integer NOT NULL,
    product_type_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3513 (class 0 OID 0)
-- Dependencies: 226
-- Name: TABLE rmt_promotion_product_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_promotion_product_type IS 'Relational master table of promotion types and product types according to the business scope and the technology scope.';


--
-- TOC entry 3514 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN rmt_promotion_product_type.prom_prod_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_product_type.prom_prod_type_id IS 'Internal identifier of the promotion_product_type';


--
-- TOC entry 3515 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN rmt_promotion_product_type.promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_product_type.promotion_type_id IS 'Promotion type id';


--
-- TOC entry 3516 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN rmt_promotion_product_type.product_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_product_type.product_type_id IS 'Product type id';


--
-- TOC entry 3517 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN rmt_promotion_product_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_product_type.start_date IS 'Start date of the current record of the promotion_product_type (historic changes)';


--
-- TOC entry 3518 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN rmt_promotion_product_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_product_type.end_date IS 'End date of the current record of the promotion_product_type (historic changes)';


--
-- TOC entry 3519 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN rmt_promotion_product_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_product_type.status_id IS 'Status id of the current promotion_product_type';


--
-- TOC entry 3520 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN rmt_promotion_product_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_product_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to the promotion_product_type';


--
-- TOC entry 3521 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN rmt_promotion_product_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_product_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the promotion_product_type';


--
-- TOC entry 3522 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN rmt_promotion_product_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_product_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3523 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN rmt_promotion_product_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_product_type.input_user IS 'User who created the record';


--
-- TOC entry 3524 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN rmt_promotion_product_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_product_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3525 (class 0 OID 0)
-- Dependencies: 226
-- Name: COLUMN rmt_promotion_product_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_product_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 227 (class 1259 OID 19180)
-- Name: rmt_promotion_service_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_promotion_service_type (
    prom_serv_type_id integer NOT NULL,
    promotion_type_id integer NOT NULL,
    service_type_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3527 (class 0 OID 0)
-- Dependencies: 227
-- Name: TABLE rmt_promotion_service_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_promotion_service_type IS 'Relational master table of promotion types and service types according to the business scope and the technology scope.';


--
-- TOC entry 3528 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN rmt_promotion_service_type.promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_service_type.promotion_type_id IS 'Promotion type id';


--
-- TOC entry 3529 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN rmt_promotion_service_type.service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_service_type.service_type_id IS 'Service type id';


--
-- TOC entry 3530 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN rmt_promotion_service_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_service_type.start_date IS 'Start date of the current record of the promotion_service_type (historic changes)';


--
-- TOC entry 3531 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN rmt_promotion_service_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_service_type.end_date IS 'End date of the current record of the promotion_service_type (historic changes)';


--
-- TOC entry 3532 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN rmt_promotion_service_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_service_type.status_id IS 'Status id of the current promotion_service_type';


--
-- TOC entry 3533 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN rmt_promotion_service_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_service_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to the promotion_service_type';


--
-- TOC entry 3534 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN rmt_promotion_service_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_service_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the promotion_service_type';


--
-- TOC entry 3535 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN rmt_promotion_service_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_service_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3536 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN rmt_promotion_service_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_service_type.input_user IS 'User who created the record';


--
-- TOC entry 3537 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN rmt_promotion_service_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_service_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3538 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN rmt_promotion_service_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_promotion_service_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 228 (class 1259 OID 19186)
-- Name: rmt_service_equipment_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_service_equipment_type (
    service_equipment_type_id integer NOT NULL,
    service_type_id integer NOT NULL,
    equipment_type_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3540 (class 0 OID 0)
-- Dependencies: 228
-- Name: TABLE rmt_service_equipment_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_service_equipment_type IS 'Relational master table of service types and equipment types according to the business scope and the technology scope.';


--
-- TOC entry 3541 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN rmt_service_equipment_type.service_equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_service_equipment_type.service_equipment_type_id IS 'Internal identifier of the service_equipment_type';


--
-- TOC entry 3542 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN rmt_service_equipment_type.service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_service_equipment_type.service_type_id IS 'Service type id';


--
-- TOC entry 3543 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN rmt_service_equipment_type.equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_service_equipment_type.equipment_type_id IS 'Equipment type id';


--
-- TOC entry 3544 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN rmt_service_equipment_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_service_equipment_type.start_date IS 'Start date of the current record of the service_equipment_type (historic changes)';


--
-- TOC entry 3545 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN rmt_service_equipment_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_service_equipment_type.end_date IS 'End date of the current record of the service_equipment_type (historic changes)';


--
-- TOC entry 3546 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN rmt_service_equipment_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_service_equipment_type.status_id IS 'Status id of the current service_equipment_type';


--
-- TOC entry 3547 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN rmt_service_equipment_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_service_equipment_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to the service_equipment_type';


--
-- TOC entry 3548 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN rmt_service_equipment_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_service_equipment_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the service_equipment_type';


--
-- TOC entry 3549 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN rmt_service_equipment_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_service_equipment_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3550 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN rmt_service_equipment_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_service_equipment_type.input_user IS 'User who created the record';


--
-- TOC entry 3551 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN rmt_service_equipment_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_service_equipment_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3552 (class 0 OID 0)
-- Dependencies: 228
-- Name: COLUMN rmt_service_equipment_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_service_equipment_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 229 (class 1259 OID 19192)
-- Name: rmt_tariff_equipment_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_tariff_equipment_type (
    tariff_equipment_type_id integer NOT NULL,
    tariff_type_id integer NOT NULL,
    equipment_type_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3554 (class 0 OID 0)
-- Dependencies: 229
-- Name: TABLE rmt_tariff_equipment_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_tariff_equipment_type IS 'Relational master table of tariff types and equipment types according to the business scope and the technology scope.';


--
-- TOC entry 3555 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN rmt_tariff_equipment_type.tariff_equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_equipment_type.tariff_equipment_type_id IS 'Internal identifier of the tariff_equipment_type';


--
-- TOC entry 3556 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN rmt_tariff_equipment_type.tariff_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_equipment_type.tariff_type_id IS 'Tariff type id';


--
-- TOC entry 3557 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN rmt_tariff_equipment_type.equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_equipment_type.equipment_type_id IS 'Equipment type id';


--
-- TOC entry 3558 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN rmt_tariff_equipment_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_equipment_type.start_date IS 'Start date of the current record of the tariff_equipment_type (historic changes)';


--
-- TOC entry 3559 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN rmt_tariff_equipment_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_equipment_type.end_date IS 'End date of the current record of the tariff_equipment_type (historic changes)';


--
-- TOC entry 3560 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN rmt_tariff_equipment_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_equipment_type.status_id IS 'Status id of the current tariff_equipment_type';


--
-- TOC entry 3561 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN rmt_tariff_equipment_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_equipment_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to the tariff_equipment_type';


--
-- TOC entry 3562 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN rmt_tariff_equipment_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_equipment_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the tariff_equipment_type';


--
-- TOC entry 3563 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN rmt_tariff_equipment_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_equipment_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3564 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN rmt_tariff_equipment_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_equipment_type.input_user IS 'User who created the record';


--
-- TOC entry 3565 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN rmt_tariff_equipment_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_equipment_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3566 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN rmt_tariff_equipment_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_equipment_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 230 (class 1259 OID 19198)
-- Name: rmt_tariff_product_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_tariff_product_type (
    tariff_product_type_id integer NOT NULL,
    tariff_type_id integer NOT NULL,
    product_type_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3568 (class 0 OID 0)
-- Dependencies: 230
-- Name: TABLE rmt_tariff_product_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_tariff_product_type IS 'Relational master table of tariff types and product types according to the business scope and the technology scope.';


--
-- TOC entry 3569 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN rmt_tariff_product_type.tariff_product_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_product_type.tariff_product_type_id IS 'Internal identifier of the tariff_product_type';


--
-- TOC entry 3570 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN rmt_tariff_product_type.tariff_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_product_type.tariff_type_id IS 'Tariff type id';


--
-- TOC entry 3571 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN rmt_tariff_product_type.product_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_product_type.product_type_id IS 'Product type id';


--
-- TOC entry 3572 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN rmt_tariff_product_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_product_type.start_date IS 'Start date of the current record of the tariff_product_type (historic changes)';


--
-- TOC entry 3573 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN rmt_tariff_product_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_product_type.end_date IS 'End date of the current record of the tariff_product_type (historic changes)';


--
-- TOC entry 3574 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN rmt_tariff_product_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_product_type.status_id IS 'Status id of the current tariff_product_type';


--
-- TOC entry 3575 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN rmt_tariff_product_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_product_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to the tariff_product_type';


--
-- TOC entry 3576 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN rmt_tariff_product_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_product_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the tariff_product_type';


--
-- TOC entry 3577 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN rmt_tariff_product_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_product_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3578 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN rmt_tariff_product_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_product_type.input_user IS 'User who created the record';


--
-- TOC entry 3579 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN rmt_tariff_product_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_product_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3580 (class 0 OID 0)
-- Dependencies: 230
-- Name: COLUMN rmt_tariff_product_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_product_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 231 (class 1259 OID 19204)
-- Name: rmt_tariff_promotion_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_tariff_promotion_type (
    tariff_promotion_type_id integer NOT NULL,
    tariff_type_id integer NOT NULL,
    promotion_type_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3582 (class 0 OID 0)
-- Dependencies: 231
-- Name: TABLE rmt_tariff_promotion_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_tariff_promotion_type IS 'Relational master table of tariff types and promotion types according to the business scope and the technology scope.';


--
-- TOC entry 3583 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN rmt_tariff_promotion_type.tariff_promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_promotion_type.tariff_promotion_type_id IS 'Internal identifier of the tariff_promotion_type';


--
-- TOC entry 3584 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN rmt_tariff_promotion_type.tariff_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_promotion_type.tariff_type_id IS 'Tariff type id';


--
-- TOC entry 3585 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN rmt_tariff_promotion_type.promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_promotion_type.promotion_type_id IS 'Promotion type id';


--
-- TOC entry 3586 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN rmt_tariff_promotion_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_promotion_type.start_date IS 'Start date of the current record of the tariff_promotion_type (historic changes)';


--
-- TOC entry 3587 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN rmt_tariff_promotion_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_promotion_type.end_date IS 'End date of the current record of the tariff_promotion_type (historic changes)';


--
-- TOC entry 3588 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN rmt_tariff_promotion_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_promotion_type.status_id IS 'Status id of the current tariff_promotion_type';


--
-- TOC entry 3589 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN rmt_tariff_promotion_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_promotion_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to the tariff_promotion_type';


--
-- TOC entry 3590 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN rmt_tariff_promotion_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_promotion_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the tariff_promotion_type';


--
-- TOC entry 3591 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN rmt_tariff_promotion_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_promotion_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3592 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN rmt_tariff_promotion_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_promotion_type.input_user IS 'User who created the record';


--
-- TOC entry 3593 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN rmt_tariff_promotion_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_promotion_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3594 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN rmt_tariff_promotion_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_promotion_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 232 (class 1259 OID 19210)
-- Name: rmt_tariff_service_type; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE rmt_tariff_service_type (
    tariff_service_type_id integer NOT NULL,
    tariff_type_id integer NOT NULL,
    service_type_id integer NOT NULL,
    start_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    end_date timestamp without time zone DEFAULT (to_timestamp('9999-12-31 23:59:59'::text, 'YYYY-MM-DD HH24:MI:SS'::text))::timestamp without time zone NOT NULL,
    status_id integer NOT NULL,
    business_scope_id integer NOT NULL,
    technology_scope_id integer NOT NULL,
    input_date timestamp without time zone DEFAULT (('now'::text)::timestamp(0) with time zone)::timestamp without time zone NOT NULL,
    input_user character varying(10) NOT NULL,
    modif_date timestamp without time zone,
    modif_user character varying(10)
);


--
-- TOC entry 3596 (class 0 OID 0)
-- Dependencies: 232
-- Name: TABLE rmt_tariff_service_type; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE rmt_tariff_service_type IS 'Relational master table of tariff types and service types according to the business scope and the technology scope.';


--
-- TOC entry 3597 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN rmt_tariff_service_type.tariff_service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_service_type.tariff_service_type_id IS 'Internal identifier of the tariff_service_type';


--
-- TOC entry 3598 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN rmt_tariff_service_type.tariff_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_service_type.tariff_type_id IS 'Tariff type id';


--
-- TOC entry 3599 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN rmt_tariff_service_type.service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_service_type.service_type_id IS 'Service type id';


--
-- TOC entry 3600 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN rmt_tariff_service_type.start_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_service_type.start_date IS 'Start date of the current record of the user (historic changes)';


--
-- TOC entry 3601 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN rmt_tariff_service_type.end_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_service_type.end_date IS 'End date of the current record of the user (historic changes)';


--
-- TOC entry 3602 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN rmt_tariff_service_type.status_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_service_type.status_id IS 'Status id of the current user';


--
-- TOC entry 3603 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN rmt_tariff_service_type.business_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_service_type.business_scope_id IS 'Business scope (corporate, personal, ...) id associated to the tariff_service_type';


--
-- TOC entry 3604 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN rmt_tariff_service_type.technology_scope_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_service_type.technology_scope_id IS 'Technology scope (fixed phone, mobile, television, ...) id associated to the tariff_service_type';


--
-- TOC entry 3605 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN rmt_tariff_service_type.input_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_service_type.input_date IS 'Date on which the record was created';


--
-- TOC entry 3606 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN rmt_tariff_service_type.input_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_service_type.input_user IS 'User who created the record';


--
-- TOC entry 3607 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN rmt_tariff_service_type.modif_date; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_service_type.modif_date IS 'Date of the last modified of the record';


--
-- TOC entry 3608 (class 0 OID 0)
-- Dependencies: 232
-- Name: COLUMN rmt_tariff_service_type.modif_user; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN rmt_tariff_service_type.modif_user IS 'User who done the last modified';


--
-- TOC entry 233 (class 1259 OID 19216)
-- Name: test_menu; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE test_menu (
    menu_id integer NOT NULL,
    menu_code character varying(20) NOT NULL,
    menu_type character varying(2) NOT NULL,
    menu_level integer NOT NULL,
    "position" integer NOT NULL,
    description character varying(255) NOT NULL,
    profile_code character varying(20) NOT NULL,
    submenu_id integer,
    status character varying(1) NOT NULL,
    page character varying(100)
);


--
-- TOC entry 234 (class 1259 OID 19219)
-- Name: test_user; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE test_user (
    user_id integer NOT NULL,
    user_code character varying(20) NOT NULL,
    profile_id integer NOT NULL,
    profile_code character varying(20) NOT NULL,
    password character varying(32) NOT NULL
);


--
-- TOC entry 235 (class 1259 OID 19222)
-- Name: tid_consumption_code_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_consumption_code_id (
    consumption_code_id integer NOT NULL
);


--
-- TOC entry 3612 (class 0 OID 0)
-- Dependencies: 235
-- Name: TABLE tid_consumption_code_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_consumption_code_id IS 'Table identifiers of consumption code id';


--
-- TOC entry 3613 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN tid_consumption_code_id.consumption_code_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_consumption_code_id.consumption_code_id IS 'Internal identifier of the consumption code. It will be referenced by other tables';


--
-- TOC entry 236 (class 1259 OID 19225)
-- Name: tid_consumption_code_id_consumption_code_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_consumption_code_id_consumption_code_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3615 (class 0 OID 0)
-- Dependencies: 236
-- Name: tid_consumption_code_id_consumption_code_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_consumption_code_id_consumption_code_id_seq OWNED BY tid_consumption_code_id.consumption_code_id;


--
-- TOC entry 237 (class 1259 OID 19227)
-- Name: tid_equipment_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_equipment_type_id (
    equipment_type_id integer NOT NULL
);


--
-- TOC entry 3616 (class 0 OID 0)
-- Dependencies: 237
-- Name: TABLE tid_equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_equipment_type_id IS 'Table identifiers of equipment type id';


--
-- TOC entry 3617 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN tid_equipment_type_id.equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_equipment_type_id.equipment_type_id IS 'Internal identifier of the equipment type. It will be referenced by other tables';


--
-- TOC entry 238 (class 1259 OID 19230)
-- Name: tid_equipment_type_id_equipment_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_equipment_type_id_equipment_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3619 (class 0 OID 0)
-- Dependencies: 238
-- Name: tid_equipment_type_id_equipment_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_equipment_type_id_equipment_type_id_seq OWNED BY tid_equipment_type_id.equipment_type_id;


--
-- TOC entry 239 (class 1259 OID 19232)
-- Name: tid_fee_code_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_fee_code_id (
    fee_code_id integer NOT NULL
);


--
-- TOC entry 3620 (class 0 OID 0)
-- Dependencies: 239
-- Name: TABLE tid_fee_code_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_fee_code_id IS 'Table identifiers of fee code id';


--
-- TOC entry 3621 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN tid_fee_code_id.fee_code_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_fee_code_id.fee_code_id IS 'Internal identifier of the fee code. It will be referenced by other tables';


--
-- TOC entry 240 (class 1259 OID 19235)
-- Name: tid_fee_code_id_fee_code_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_fee_code_id_fee_code_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3623 (class 0 OID 0)
-- Dependencies: 240
-- Name: tid_fee_code_id_fee_code_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_fee_code_id_fee_code_id_seq OWNED BY tid_fee_code_id.fee_code_id;


--
-- TOC entry 241 (class 1259 OID 19237)
-- Name: tid_fee_equipment_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_fee_equipment_type_id (
    fee_equipment_type_id integer NOT NULL
);


--
-- TOC entry 3624 (class 0 OID 0)
-- Dependencies: 241
-- Name: TABLE tid_fee_equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_fee_equipment_type_id IS 'Table identifiers of fee_equipment type id (to the relationship between fee and equipment types)';


--
-- TOC entry 3625 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN tid_fee_equipment_type_id.fee_equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_fee_equipment_type_id.fee_equipment_type_id IS 'Internal identifier of the fee_equipment type (to the relationship between fee and equipment types). It will be referenced by other tables';


--
-- TOC entry 242 (class 1259 OID 19240)
-- Name: tid_fee_equipment_type_id_fee_equipment_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_fee_equipment_type_id_fee_equipment_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3627 (class 0 OID 0)
-- Dependencies: 242
-- Name: tid_fee_equipment_type_id_fee_equipment_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_fee_equipment_type_id_fee_equipment_type_id_seq OWNED BY tid_fee_equipment_type_id.fee_equipment_type_id;


--
-- TOC entry 243 (class 1259 OID 19242)
-- Name: tid_fee_product_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_fee_product_type_id (
    fee_product_type_id integer NOT NULL
);


--
-- TOC entry 3628 (class 0 OID 0)
-- Dependencies: 243
-- Name: TABLE tid_fee_product_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_fee_product_type_id IS 'Table identifiers of fee_product type id (to the relationship between fee and product types)';


--
-- TOC entry 3629 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN tid_fee_product_type_id.fee_product_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_fee_product_type_id.fee_product_type_id IS 'Internal identifier of the fee_product type (to the relationship between fee and product types). It will be referenced by other tables';


--
-- TOC entry 244 (class 1259 OID 19245)
-- Name: tid_fee_product_type_id_fee_product_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_fee_product_type_id_fee_product_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3631 (class 0 OID 0)
-- Dependencies: 244
-- Name: tid_fee_product_type_id_fee_product_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_fee_product_type_id_fee_product_type_id_seq OWNED BY tid_fee_product_type_id.fee_product_type_id;


--
-- TOC entry 245 (class 1259 OID 19247)
-- Name: tid_fee_promotion_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_fee_promotion_type_id (
    fee_promotion_type_id integer NOT NULL
);


--
-- TOC entry 3632 (class 0 OID 0)
-- Dependencies: 245
-- Name: TABLE tid_fee_promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_fee_promotion_type_id IS 'Table identifiers of fee_promotion type id (to the relationship between fee and promotion types)';


--
-- TOC entry 3633 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN tid_fee_promotion_type_id.fee_promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_fee_promotion_type_id.fee_promotion_type_id IS 'Internal identifier of the fee_promotion type (to the relationship between fee and promotion types). It will be referenced by other tables';


--
-- TOC entry 246 (class 1259 OID 19250)
-- Name: tid_fee_promotion_type_id_fee_promotion_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_fee_promotion_type_id_fee_promotion_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3635 (class 0 OID 0)
-- Dependencies: 246
-- Name: tid_fee_promotion_type_id_fee_promotion_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_fee_promotion_type_id_fee_promotion_type_id_seq OWNED BY tid_fee_promotion_type_id.fee_promotion_type_id;


--
-- TOC entry 247 (class 1259 OID 19252)
-- Name: tid_fee_service_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_fee_service_type_id (
    fee_service_type_id integer NOT NULL
);


--
-- TOC entry 3636 (class 0 OID 0)
-- Dependencies: 247
-- Name: TABLE tid_fee_service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_fee_service_type_id IS 'Table identifiers of fee_service type id (to the relationship between fee and service types)';


--
-- TOC entry 3637 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN tid_fee_service_type_id.fee_service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_fee_service_type_id.fee_service_type_id IS 'Internal identifier of the fee_service type (to the relationship between fee and service types). It will be referenced by other tables';


--
-- TOC entry 248 (class 1259 OID 19255)
-- Name: tid_fee_service_type_id_fee_service_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_fee_service_type_id_fee_service_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3639 (class 0 OID 0)
-- Dependencies: 248
-- Name: tid_fee_service_type_id_fee_service_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_fee_service_type_id_fee_service_type_id_seq OWNED BY tid_fee_service_type_id.fee_service_type_id;


--
-- TOC entry 249 (class 1259 OID 19257)
-- Name: tid_plan_charge_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_plan_charge_type_id (
    plan_charge_type_id integer NOT NULL
);


--
-- TOC entry 3640 (class 0 OID 0)
-- Dependencies: 249
-- Name: TABLE tid_plan_charge_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_plan_charge_type_id IS 'Table identifiers of plan_charge type id (to the relationship between plan and charge types)';


--
-- TOC entry 3641 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN tid_plan_charge_type_id.plan_charge_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_plan_charge_type_id.plan_charge_type_id IS 'Internal identifier of the plan_charge type (to the relationship between plan and charge types). It will be referenced by other tables';


--
-- TOC entry 250 (class 1259 OID 19260)
-- Name: tid_plan_charge_type_id_plan_charge_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_plan_charge_type_id_plan_charge_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3643 (class 0 OID 0)
-- Dependencies: 250
-- Name: tid_plan_charge_type_id_plan_charge_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_plan_charge_type_id_plan_charge_type_id_seq OWNED BY tid_plan_charge_type_id.plan_charge_type_id;


--
-- TOC entry 251 (class 1259 OID 19262)
-- Name: tid_plan_discount_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_plan_discount_type_id (
    plan_discount_type_id integer NOT NULL
);


--
-- TOC entry 3644 (class 0 OID 0)
-- Dependencies: 251
-- Name: TABLE tid_plan_discount_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_plan_discount_type_id IS 'Table identifiers of plan_discount type id (to the relationship between plan and discount types)';


--
-- TOC entry 3645 (class 0 OID 0)
-- Dependencies: 251
-- Name: COLUMN tid_plan_discount_type_id.plan_discount_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_plan_discount_type_id.plan_discount_type_id IS 'Internal identifier of the plan_discount type (to the relationship between plan and discount types). It will be referenced by other tables';


--
-- TOC entry 252 (class 1259 OID 19265)
-- Name: tid_plan_discount_type_id_plan_discount_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_plan_discount_type_id_plan_discount_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3647 (class 0 OID 0)
-- Dependencies: 252
-- Name: tid_plan_discount_type_id_plan_discount_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_plan_discount_type_id_plan_discount_type_id_seq OWNED BY tid_plan_discount_type_id.plan_discount_type_id;


--
-- TOC entry 253 (class 1259 OID 19267)
-- Name: tid_plan_prerreq_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_plan_prerreq_type_id (
    plan_prerreq_type_id integer NOT NULL
);


--
-- TOC entry 3648 (class 0 OID 0)
-- Dependencies: 253
-- Name: TABLE tid_plan_prerreq_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_plan_prerreq_type_id IS 'Table identifiers of plan_prerreq type id (to the relationship between plan and prerrequisite types)';


--
-- TOC entry 3649 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN tid_plan_prerreq_type_id.plan_prerreq_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_plan_prerreq_type_id.plan_prerreq_type_id IS 'Internal identifier of the plan_prerreq type (to the relationship between plan and prerrequisite types). It will be referenced by other tables';


--
-- TOC entry 254 (class 1259 OID 19270)
-- Name: tid_plan_prerreq_type_id_plan_prerreq_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_plan_prerreq_type_id_plan_prerreq_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3651 (class 0 OID 0)
-- Dependencies: 254
-- Name: tid_plan_prerreq_type_id_plan_prerreq_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_plan_prerreq_type_id_plan_prerreq_type_id_seq OWNED BY tid_plan_prerreq_type_id.plan_prerreq_type_id;


--
-- TOC entry 255 (class 1259 OID 19272)
-- Name: tid_plan_promotion_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_plan_promotion_type_id (
    plan_promotion_type_id integer NOT NULL
);


--
-- TOC entry 3652 (class 0 OID 0)
-- Dependencies: 255
-- Name: TABLE tid_plan_promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_plan_promotion_type_id IS 'Table identifiers of plan_promotion type id (to the relationship between promotion and plan types)';


--
-- TOC entry 3653 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN tid_plan_promotion_type_id.plan_promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_plan_promotion_type_id.plan_promotion_type_id IS 'Internal identifier of the plan_promotion type (to the relationship between promotion and plan types). It will be referenced by other tables';


--
-- TOC entry 256 (class 1259 OID 19275)
-- Name: tid_plan_promotion_type_id_plan_promotion_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_plan_promotion_type_id_plan_promotion_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3655 (class 0 OID 0)
-- Dependencies: 256
-- Name: tid_plan_promotion_type_id_plan_promotion_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_plan_promotion_type_id_plan_promotion_type_id_seq OWNED BY tid_plan_promotion_type_id.plan_promotion_type_id;


--
-- TOC entry 257 (class 1259 OID 19277)
-- Name: tid_plan_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_plan_type_id (
    plan_type_id integer NOT NULL
);


--
-- TOC entry 3656 (class 0 OID 0)
-- Dependencies: 257
-- Name: TABLE tid_plan_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_plan_type_id IS 'Table identifiers of promotion plan type id';


--
-- TOC entry 3657 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN tid_plan_type_id.plan_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_plan_type_id.plan_type_id IS 'Internal identifier of the promotion plan type. It will be referenced by other tables';


--
-- TOC entry 258 (class 1259 OID 19280)
-- Name: tid_plan_type_id_plan_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_plan_type_id_plan_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3659 (class 0 OID 0)
-- Dependencies: 258
-- Name: tid_plan_type_id_plan_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_plan_type_id_plan_type_id_seq OWNED BY tid_plan_type_id.plan_type_id;


--
-- TOC entry 259 (class 1259 OID 19282)
-- Name: tid_product_service_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_product_service_type_id (
    product_service_type_id integer NOT NULL
);


--
-- TOC entry 3660 (class 0 OID 0)
-- Dependencies: 259
-- Name: TABLE tid_product_service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_product_service_type_id IS 'Table identifiers of product_service type id (to the relationship between product and service types)';


--
-- TOC entry 3661 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN tid_product_service_type_id.product_service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_product_service_type_id.product_service_type_id IS 'Internal identifier of the product_service type (to the relationship between product and service types). It will be referenced by other tables';


--
-- TOC entry 260 (class 1259 OID 19285)
-- Name: tid_product_service_type_id_product_service_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_product_service_type_id_product_service_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3663 (class 0 OID 0)
-- Dependencies: 260
-- Name: tid_product_service_type_id_product_service_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_product_service_type_id_product_service_type_id_seq OWNED BY tid_product_service_type_id.product_service_type_id;


--
-- TOC entry 261 (class 1259 OID 19287)
-- Name: tid_product_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_product_type_id (
    product_type_id integer NOT NULL
);


--
-- TOC entry 3664 (class 0 OID 0)
-- Dependencies: 261
-- Name: TABLE tid_product_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_product_type_id IS 'Table identifiers of product type id';


--
-- TOC entry 3665 (class 0 OID 0)
-- Dependencies: 261
-- Name: COLUMN tid_product_type_id.product_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_product_type_id.product_type_id IS 'Internal identifier of the product type. It will be referenced by other tables';


--
-- TOC entry 262 (class 1259 OID 19290)
-- Name: tid_product_type_id_product_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_product_type_id_product_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3667 (class 0 OID 0)
-- Dependencies: 262
-- Name: tid_product_type_id_product_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_product_type_id_product_type_id_seq OWNED BY tid_product_type_id.product_type_id;


--
-- TOC entry 263 (class 1259 OID 19292)
-- Name: tid_profile_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_profile_id (
    profile_id integer NOT NULL
);


--
-- TOC entry 3669 (class 0 OID 0)
-- Dependencies: 263
-- Name: TABLE tid_profile_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_profile_id IS 'Table identifiers of consumption type id';


--
-- TOC entry 3670 (class 0 OID 0)
-- Dependencies: 263
-- Name: COLUMN tid_profile_id.profile_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_profile_id.profile_id IS 'Internal identifier of the consumption type. It will be referenced by other tables';


--
-- TOC entry 264 (class 1259 OID 19295)
-- Name: tid_profile_id_profile_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_profile_id_profile_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3672 (class 0 OID 0)
-- Dependencies: 264
-- Name: tid_profile_id_profile_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_profile_id_profile_id_seq OWNED BY tid_profile_id.profile_id;


--
-- TOC entry 265 (class 1259 OID 19297)
-- Name: tid_promotion_equipment_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_promotion_equipment_type_id (
    prom_equip_type_id integer NOT NULL
);


--
-- TOC entry 3674 (class 0 OID 0)
-- Dependencies: 265
-- Name: TABLE tid_promotion_equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_promotion_equipment_type_id IS 'Table identifiers of promotion_equipment type id (to the relationship between promotion and equipment types)';


--
-- TOC entry 3675 (class 0 OID 0)
-- Dependencies: 265
-- Name: COLUMN tid_promotion_equipment_type_id.prom_equip_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_promotion_equipment_type_id.prom_equip_type_id IS 'Internal identifier of the promotion_equipment type (to the relationship between promotion and equipment types). It will be referenced by other tables';


--
-- TOC entry 266 (class 1259 OID 19300)
-- Name: tid_promotion_equipment_type_id_prom_equip_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_promotion_equipment_type_id_prom_equip_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3677 (class 0 OID 0)
-- Dependencies: 266
-- Name: tid_promotion_equipment_type_id_prom_equip_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_promotion_equipment_type_id_prom_equip_type_id_seq OWNED BY tid_promotion_equipment_type_id.prom_equip_type_id;


--
-- TOC entry 267 (class 1259 OID 19302)
-- Name: tid_promotion_product_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_promotion_product_type_id (
    prom_prod_type_id integer NOT NULL
);


--
-- TOC entry 3678 (class 0 OID 0)
-- Dependencies: 267
-- Name: TABLE tid_promotion_product_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_promotion_product_type_id IS 'Table identifiers of promotion_product type id (to the relationship between promotion and product types)';


--
-- TOC entry 3679 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN tid_promotion_product_type_id.prom_prod_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_promotion_product_type_id.prom_prod_type_id IS 'Internal identifier of the promotion_product type (to the relationship between promotion and product types). It will be referenced by other tables';


--
-- TOC entry 268 (class 1259 OID 19305)
-- Name: tid_promotion_product_type_id_prom_prod_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_promotion_product_type_id_prom_prod_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3681 (class 0 OID 0)
-- Dependencies: 268
-- Name: tid_promotion_product_type_id_prom_prod_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_promotion_product_type_id_prom_prod_type_id_seq OWNED BY tid_promotion_product_type_id.prom_prod_type_id;


--
-- TOC entry 269 (class 1259 OID 19307)
-- Name: tid_promotion_promotion_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_promotion_promotion_type_id (
    prom_prom_type_id integer NOT NULL
);


--
-- TOC entry 3682 (class 0 OID 0)
-- Dependencies: 269
-- Name: TABLE tid_promotion_promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_promotion_promotion_type_id IS 'Table identifiers of promotion_promotion type id (to the relationship between promotion and promotion types)';


--
-- TOC entry 3683 (class 0 OID 0)
-- Dependencies: 269
-- Name: COLUMN tid_promotion_promotion_type_id.prom_prom_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_promotion_promotion_type_id.prom_prom_type_id IS 'Internal identifier of the promotion_promotion type (to the relationship between promotion and promotion types). It will be referenced by other tables';


--
-- TOC entry 270 (class 1259 OID 19310)
-- Name: tid_promotion_promotion_type_id_prom_prom_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_promotion_promotion_type_id_prom_prom_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3685 (class 0 OID 0)
-- Dependencies: 270
-- Name: tid_promotion_promotion_type_id_prom_prom_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_promotion_promotion_type_id_prom_prom_type_id_seq OWNED BY tid_promotion_promotion_type_id.prom_prom_type_id;


--
-- TOC entry 271 (class 1259 OID 19312)
-- Name: tid_promotion_service_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_promotion_service_type_id (
    prom_serv_type_id integer NOT NULL
);


--
-- TOC entry 3686 (class 0 OID 0)
-- Dependencies: 271
-- Name: TABLE tid_promotion_service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_promotion_service_type_id IS 'Table identifiers of promotion_service type id (to the relationship between promotion and service types)';


--
-- TOC entry 3687 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN tid_promotion_service_type_id.prom_serv_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_promotion_service_type_id.prom_serv_type_id IS 'Internal identifier of the promotion_service type (to the relationship between promotion and service types). It will be referenced by other tables';


--
-- TOC entry 272 (class 1259 OID 19315)
-- Name: tid_promotion_service_type_id_prom_serv_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_promotion_service_type_id_prom_serv_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3689 (class 0 OID 0)
-- Dependencies: 272
-- Name: tid_promotion_service_type_id_prom_serv_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_promotion_service_type_id_prom_serv_type_id_seq OWNED BY tid_promotion_service_type_id.prom_serv_type_id;


--
-- TOC entry 273 (class 1259 OID 19317)
-- Name: tid_promotion_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_promotion_type_id (
    promotion_type_id integer NOT NULL
);


--
-- TOC entry 3690 (class 0 OID 0)
-- Dependencies: 273
-- Name: TABLE tid_promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_promotion_type_id IS 'Table identifiers of promotion type id';


--
-- TOC entry 3691 (class 0 OID 0)
-- Dependencies: 273
-- Name: COLUMN tid_promotion_type_id.promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_promotion_type_id.promotion_type_id IS 'Internal identifier of the promotion type. It will be referenced by other tables';


--
-- TOC entry 274 (class 1259 OID 19320)
-- Name: tid_promotion_type_id_promotion_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_promotion_type_id_promotion_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3693 (class 0 OID 0)
-- Dependencies: 274
-- Name: tid_promotion_type_id_promotion_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_promotion_type_id_promotion_type_id_seq OWNED BY tid_promotion_type_id.promotion_type_id;


--
-- TOC entry 275 (class 1259 OID 19322)
-- Name: tid_service_equipment_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_service_equipment_type_id (
    service_equipment_type_id integer NOT NULL
);


--
-- TOC entry 3694 (class 0 OID 0)
-- Dependencies: 275
-- Name: TABLE tid_service_equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_service_equipment_type_id IS 'Table identifiers of service_equipment type id (to the relationship between service and equipment types)';


--
-- TOC entry 3695 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN tid_service_equipment_type_id.service_equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_service_equipment_type_id.service_equipment_type_id IS 'Internal identifier of the service_equipment type (to the relationship between service and equipment types). It will be referenced by other tables';


--
-- TOC entry 276 (class 1259 OID 19325)
-- Name: tid_service_equipment_type_id_service_equipment_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_service_equipment_type_id_service_equipment_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3697 (class 0 OID 0)
-- Dependencies: 276
-- Name: tid_service_equipment_type_id_service_equipment_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_service_equipment_type_id_service_equipment_type_id_seq OWNED BY tid_service_equipment_type_id.service_equipment_type_id;


--
-- TOC entry 277 (class 1259 OID 19327)
-- Name: tid_service_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_service_type_id (
    service_type_id integer NOT NULL
);


--
-- TOC entry 3698 (class 0 OID 0)
-- Dependencies: 277
-- Name: TABLE tid_service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_service_type_id IS 'Table identifiers of service type id';


--
-- TOC entry 3699 (class 0 OID 0)
-- Dependencies: 277
-- Name: COLUMN tid_service_type_id.service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_service_type_id.service_type_id IS 'Internal identifier of the service type. It will be referenced by other tables';


--
-- TOC entry 278 (class 1259 OID 19330)
-- Name: tid_service_type_id_service_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_service_type_id_service_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3701 (class 0 OID 0)
-- Dependencies: 278
-- Name: tid_service_type_id_service_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_service_type_id_service_type_id_seq OWNED BY tid_service_type_id.service_type_id;


--
-- TOC entry 279 (class 1259 OID 19332)
-- Name: tid_tariff_equipment_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_tariff_equipment_type_id (
    tariff_equipment_type_id integer NOT NULL
);


--
-- TOC entry 3703 (class 0 OID 0)
-- Dependencies: 279
-- Name: TABLE tid_tariff_equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_tariff_equipment_type_id IS 'Table identifiers of tariff_equipment type id (to the relationship between tariff and equipment types)';


--
-- TOC entry 3704 (class 0 OID 0)
-- Dependencies: 279
-- Name: COLUMN tid_tariff_equipment_type_id.tariff_equipment_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_tariff_equipment_type_id.tariff_equipment_type_id IS 'Internal identifier of the tariff_equipment type (to the relationship between tariff and equipment types). It will be referenced by other tables';


--
-- TOC entry 280 (class 1259 OID 19335)
-- Name: tid_tariff_equipment_type_id_tariff_equipment_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_tariff_equipment_type_id_tariff_equipment_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3706 (class 0 OID 0)
-- Dependencies: 280
-- Name: tid_tariff_equipment_type_id_tariff_equipment_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_tariff_equipment_type_id_tariff_equipment_type_id_seq OWNED BY tid_tariff_equipment_type_id.tariff_equipment_type_id;


--
-- TOC entry 281 (class 1259 OID 19337)
-- Name: tid_tariff_product_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_tariff_product_type_id (
    tariff_product_type_id integer NOT NULL
);


--
-- TOC entry 3707 (class 0 OID 0)
-- Dependencies: 281
-- Name: TABLE tid_tariff_product_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_tariff_product_type_id IS 'Table identifiers of tariff_product type id (to the relationship between tariff and product types)';


--
-- TOC entry 3708 (class 0 OID 0)
-- Dependencies: 281
-- Name: COLUMN tid_tariff_product_type_id.tariff_product_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_tariff_product_type_id.tariff_product_type_id IS 'Internal identifier of the tariff_product type (to the relationship between tariff and product types). It will be referenced by other tables';


--
-- TOC entry 282 (class 1259 OID 19340)
-- Name: tid_tariff_product_type_id_tariff_product_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_tariff_product_type_id_tariff_product_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3710 (class 0 OID 0)
-- Dependencies: 282
-- Name: tid_tariff_product_type_id_tariff_product_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_tariff_product_type_id_tariff_product_type_id_seq OWNED BY tid_tariff_product_type_id.tariff_product_type_id;


--
-- TOC entry 283 (class 1259 OID 19342)
-- Name: tid_tariff_promotion_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_tariff_promotion_type_id (
    tariff_promotion_type_id integer NOT NULL
);


--
-- TOC entry 3711 (class 0 OID 0)
-- Dependencies: 283
-- Name: TABLE tid_tariff_promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_tariff_promotion_type_id IS 'Table identifiers of tariff_promotion type id (to the relationship between tariff and promotion types)';


--
-- TOC entry 3712 (class 0 OID 0)
-- Dependencies: 283
-- Name: COLUMN tid_tariff_promotion_type_id.tariff_promotion_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_tariff_promotion_type_id.tariff_promotion_type_id IS 'Internal identifier of the tariff_promotion type (to the relationship between tariff and promotion types). It will be referenced by other tables';


--
-- TOC entry 284 (class 1259 OID 19345)
-- Name: tid_tariff_promotion_type_id_tariff_promotion_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_tariff_promotion_type_id_tariff_promotion_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3714 (class 0 OID 0)
-- Dependencies: 284
-- Name: tid_tariff_promotion_type_id_tariff_promotion_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_tariff_promotion_type_id_tariff_promotion_type_id_seq OWNED BY tid_tariff_promotion_type_id.tariff_promotion_type_id;


--
-- TOC entry 285 (class 1259 OID 19347)
-- Name: tid_tariff_service_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_tariff_service_type_id (
    tariff_service_type_id integer NOT NULL
);


--
-- TOC entry 3715 (class 0 OID 0)
-- Dependencies: 285
-- Name: TABLE tid_tariff_service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_tariff_service_type_id IS 'Table identifiers of tariff_service type id (to the relationship between tariff and service types)';


--
-- TOC entry 3716 (class 0 OID 0)
-- Dependencies: 285
-- Name: COLUMN tid_tariff_service_type_id.tariff_service_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_tariff_service_type_id.tariff_service_type_id IS 'Internal identifier of the tariff_service type (to the relationship between tariff and service types). It will be referenced by other tables';


--
-- TOC entry 286 (class 1259 OID 19350)
-- Name: tid_tariff_service_type_id_tariff_service_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_tariff_service_type_id_tariff_service_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3718 (class 0 OID 0)
-- Dependencies: 286
-- Name: tid_tariff_service_type_id_tariff_service_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_tariff_service_type_id_tariff_service_type_id_seq OWNED BY tid_tariff_service_type_id.tariff_service_type_id;


--
-- TOC entry 287 (class 1259 OID 19352)
-- Name: tid_tariff_type_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_tariff_type_id (
    tariff_type_id integer NOT NULL
);


--
-- TOC entry 3719 (class 0 OID 0)
-- Dependencies: 287
-- Name: TABLE tid_tariff_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_tariff_type_id IS 'Table identifiers of tariff type id';


--
-- TOC entry 3720 (class 0 OID 0)
-- Dependencies: 287
-- Name: COLUMN tid_tariff_type_id.tariff_type_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_tariff_type_id.tariff_type_id IS 'Internal identifier of the tariff type. It will be referenced by other tables';


--
-- TOC entry 288 (class 1259 OID 19355)
-- Name: tid_tariff_type_id_tariff_type_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_tariff_type_id_tariff_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3722 (class 0 OID 0)
-- Dependencies: 288
-- Name: tid_tariff_type_id_tariff_type_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_tariff_type_id_tariff_type_id_seq OWNED BY tid_tariff_type_id.tariff_type_id;


--
-- TOC entry 289 (class 1259 OID 19357)
-- Name: tid_user_id; Type: TABLE; Schema: billing; Owner: billing_admin
--

CREATE TABLE tid_user_id (
    user_id integer NOT NULL
);


--
-- TOC entry 3723 (class 0 OID 0)
-- Dependencies: 289
-- Name: TABLE tid_user_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON TABLE tid_user_id IS 'Table identifiers of application user';


--
-- TOC entry 3724 (class 0 OID 0)
-- Dependencies: 289
-- Name: COLUMN tid_user_id.user_id; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON COLUMN tid_user_id.user_id IS 'Internal identifier of an application user. It will be referenced by other tables';


--
-- TOC entry 290 (class 1259 OID 19360)
-- Name: tid_user_id_user_id_seq; Type: SEQUENCE; Schema: billing; Owner: billing_admin
--

CREATE SEQUENCE tid_user_id_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3726 (class 0 OID 0)
-- Dependencies: 290
-- Name: tid_user_id_user_id_seq; Type: SEQUENCE OWNED BY; Schema: billing; Owner: billing_admin
--

ALTER SEQUENCE tid_user_id_user_id_seq OWNED BY tid_user_id.user_id;


--
-- TOC entry 291 (class 1259 OID 19362)
-- Name: v_user_profile; Type: VIEW; Schema: billing; Owner: billing_admin
--

CREATE VIEW v_user_profile AS
 SELECT u.user_id,
    u.user_code,
    u.user_name,
    u.password,
    u.profile_id,
    ( SELECT p.profile_code
           FROM mt_profile p
          WHERE (p.profile_id = u.profile_id)) AS profile_code,
    ( SELECT p.description
           FROM mt_profile p
          WHERE (p.profile_id = u.profile_id)) AS profile_description
   FROM it_user u
  WHERE ((('now'::text)::date >= u.start_date) AND (('now'::text)::date <= u.end_date) AND (u.status_id = 1));


--
-- TOC entry 3728 (class 0 OID 0)
-- Dependencies: 291
-- Name: VIEW v_user_profile; Type: COMMENT; Schema: billing; Owner: billing_admin
--

COMMENT ON VIEW v_user_profile IS 'View of the current relationship between users and profiles';


--
-- TOC entry 2472 (class 2604 OID 19366)
-- Name: mt_application_level application_level_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_application_level ALTER COLUMN application_level_id SET DEFAULT nextval('mt_application_level_application_level_id_seq'::regclass);


--
-- TOC entry 2473 (class 2604 OID 19367)
-- Name: mt_application_unit application_unit_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_application_unit ALTER COLUMN application_unit_id SET DEFAULT nextval('mt_application_unit_application_unit_id_seq'::regclass);


--
-- TOC entry 2474 (class 2604 OID 19368)
-- Name: mt_business_scope business_scope_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_business_scope ALTER COLUMN business_scope_id SET DEFAULT nextval('mt_business_scope_business_scope_id_seq'::regclass);


--
-- TOC entry 2478 (class 2604 OID 19369)
-- Name: mt_consumption_type consumption_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_consumption_type ALTER COLUMN consumption_type_id SET DEFAULT nextval('mt_consumption_type_consumption_type_id_seq'::regclass);


--
-- TOC entry 2479 (class 2604 OID 19370)
-- Name: mt_discount_concept discount_concept_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_discount_concept ALTER COLUMN discount_concept_id SET DEFAULT nextval('mt_discount_concept_discount_concept_id_seq'::regclass);


--
-- TOC entry 2480 (class 2604 OID 19371)
-- Name: mt_discount_type discount_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_discount_type ALTER COLUMN discount_type_id SET DEFAULT nextval('mt_discount_type_discount_type_id_seq'::regclass);


--
-- TOC entry 2481 (class 2604 OID 19372)
-- Name: mt_entity_type entity_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_entity_type ALTER COLUMN entity_type_id SET DEFAULT nextval('mt_entity_type_entity_type_id_seq'::regclass);


--
-- TOC entry 2504 (class 2604 OID 19373)
-- Name: mt_recurrence recurrence_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_recurrence ALTER COLUMN recurrence_id SET DEFAULT nextval('mt_recurrence_recurrence_id_seq'::regclass);


--
-- TOC entry 2509 (class 2604 OID 19374)
-- Name: mt_status status_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_status ALTER COLUMN status_id SET DEFAULT nextval('mt_status_status_id_seq'::regclass);


--
-- TOC entry 2514 (class 2604 OID 19375)
-- Name: mt_technology_scope technology_scope_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_technology_scope ALTER COLUMN technology_scope_id SET DEFAULT nextval('mt_technology_scope_technology_scope_id_seq'::regclass);


--
-- TOC entry 2569 (class 2604 OID 19376)
-- Name: tid_consumption_code_id consumption_code_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_consumption_code_id ALTER COLUMN consumption_code_id SET DEFAULT nextval('tid_consumption_code_id_consumption_code_id_seq'::regclass);


--
-- TOC entry 2570 (class 2604 OID 19377)
-- Name: tid_equipment_type_id equipment_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_equipment_type_id ALTER COLUMN equipment_type_id SET DEFAULT nextval('tid_equipment_type_id_equipment_type_id_seq'::regclass);


--
-- TOC entry 2571 (class 2604 OID 19378)
-- Name: tid_fee_code_id fee_code_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_fee_code_id ALTER COLUMN fee_code_id SET DEFAULT nextval('tid_fee_code_id_fee_code_id_seq'::regclass);


--
-- TOC entry 2572 (class 2604 OID 19379)
-- Name: tid_fee_equipment_type_id fee_equipment_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_fee_equipment_type_id ALTER COLUMN fee_equipment_type_id SET DEFAULT nextval('tid_fee_equipment_type_id_fee_equipment_type_id_seq'::regclass);


--
-- TOC entry 2573 (class 2604 OID 19380)
-- Name: tid_fee_product_type_id fee_product_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_fee_product_type_id ALTER COLUMN fee_product_type_id SET DEFAULT nextval('tid_fee_product_type_id_fee_product_type_id_seq'::regclass);


--
-- TOC entry 2574 (class 2604 OID 19381)
-- Name: tid_fee_promotion_type_id fee_promotion_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_fee_promotion_type_id ALTER COLUMN fee_promotion_type_id SET DEFAULT nextval('tid_fee_promotion_type_id_fee_promotion_type_id_seq'::regclass);


--
-- TOC entry 2575 (class 2604 OID 19382)
-- Name: tid_fee_service_type_id fee_service_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_fee_service_type_id ALTER COLUMN fee_service_type_id SET DEFAULT nextval('tid_fee_service_type_id_fee_service_type_id_seq'::regclass);


--
-- TOC entry 2576 (class 2604 OID 19383)
-- Name: tid_plan_charge_type_id plan_charge_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_plan_charge_type_id ALTER COLUMN plan_charge_type_id SET DEFAULT nextval('tid_plan_charge_type_id_plan_charge_type_id_seq'::regclass);


--
-- TOC entry 2577 (class 2604 OID 19384)
-- Name: tid_plan_discount_type_id plan_discount_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_plan_discount_type_id ALTER COLUMN plan_discount_type_id SET DEFAULT nextval('tid_plan_discount_type_id_plan_discount_type_id_seq'::regclass);


--
-- TOC entry 2578 (class 2604 OID 19385)
-- Name: tid_plan_prerreq_type_id plan_prerreq_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_plan_prerreq_type_id ALTER COLUMN plan_prerreq_type_id SET DEFAULT nextval('tid_plan_prerreq_type_id_plan_prerreq_type_id_seq'::regclass);


--
-- TOC entry 2579 (class 2604 OID 19386)
-- Name: tid_plan_promotion_type_id plan_promotion_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_plan_promotion_type_id ALTER COLUMN plan_promotion_type_id SET DEFAULT nextval('tid_plan_promotion_type_id_plan_promotion_type_id_seq'::regclass);


--
-- TOC entry 2580 (class 2604 OID 19387)
-- Name: tid_plan_type_id plan_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_plan_type_id ALTER COLUMN plan_type_id SET DEFAULT nextval('tid_plan_type_id_plan_type_id_seq'::regclass);


--
-- TOC entry 2581 (class 2604 OID 19388)
-- Name: tid_product_service_type_id product_service_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_product_service_type_id ALTER COLUMN product_service_type_id SET DEFAULT nextval('tid_product_service_type_id_product_service_type_id_seq'::regclass);


--
-- TOC entry 2582 (class 2604 OID 19389)
-- Name: tid_product_type_id product_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_product_type_id ALTER COLUMN product_type_id SET DEFAULT nextval('tid_product_type_id_product_type_id_seq'::regclass);


--
-- TOC entry 2583 (class 2604 OID 19390)
-- Name: tid_profile_id profile_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_profile_id ALTER COLUMN profile_id SET DEFAULT nextval('tid_profile_id_profile_id_seq'::regclass);


--
-- TOC entry 2584 (class 2604 OID 19391)
-- Name: tid_promotion_equipment_type_id prom_equip_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_promotion_equipment_type_id ALTER COLUMN prom_equip_type_id SET DEFAULT nextval('tid_promotion_equipment_type_id_prom_equip_type_id_seq'::regclass);


--
-- TOC entry 2585 (class 2604 OID 19392)
-- Name: tid_promotion_product_type_id prom_prod_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_promotion_product_type_id ALTER COLUMN prom_prod_type_id SET DEFAULT nextval('tid_promotion_product_type_id_prom_prod_type_id_seq'::regclass);


--
-- TOC entry 2586 (class 2604 OID 19393)
-- Name: tid_promotion_promotion_type_id prom_prom_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_promotion_promotion_type_id ALTER COLUMN prom_prom_type_id SET DEFAULT nextval('tid_promotion_promotion_type_id_prom_prom_type_id_seq'::regclass);


--
-- TOC entry 2587 (class 2604 OID 19394)
-- Name: tid_promotion_service_type_id prom_serv_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_promotion_service_type_id ALTER COLUMN prom_serv_type_id SET DEFAULT nextval('tid_promotion_service_type_id_prom_serv_type_id_seq'::regclass);


--
-- TOC entry 2588 (class 2604 OID 19395)
-- Name: tid_promotion_type_id promotion_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_promotion_type_id ALTER COLUMN promotion_type_id SET DEFAULT nextval('tid_promotion_type_id_promotion_type_id_seq'::regclass);


--
-- TOC entry 2589 (class 2604 OID 19396)
-- Name: tid_service_equipment_type_id service_equipment_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_service_equipment_type_id ALTER COLUMN service_equipment_type_id SET DEFAULT nextval('tid_service_equipment_type_id_service_equipment_type_id_seq'::regclass);


--
-- TOC entry 2590 (class 2604 OID 19397)
-- Name: tid_service_type_id service_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_service_type_id ALTER COLUMN service_type_id SET DEFAULT nextval('tid_service_type_id_service_type_id_seq'::regclass);


--
-- TOC entry 2591 (class 2604 OID 19398)
-- Name: tid_tariff_equipment_type_id tariff_equipment_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_tariff_equipment_type_id ALTER COLUMN tariff_equipment_type_id SET DEFAULT nextval('tid_tariff_equipment_type_id_tariff_equipment_type_id_seq'::regclass);


--
-- TOC entry 2592 (class 2604 OID 19399)
-- Name: tid_tariff_product_type_id tariff_product_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_tariff_product_type_id ALTER COLUMN tariff_product_type_id SET DEFAULT nextval('tid_tariff_product_type_id_tariff_product_type_id_seq'::regclass);


--
-- TOC entry 2593 (class 2604 OID 19400)
-- Name: tid_tariff_promotion_type_id tariff_promotion_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_tariff_promotion_type_id ALTER COLUMN tariff_promotion_type_id SET DEFAULT nextval('tid_tariff_promotion_type_id_tariff_promotion_type_id_seq'::regclass);


--
-- TOC entry 2594 (class 2604 OID 19401)
-- Name: tid_tariff_service_type_id tariff_service_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_tariff_service_type_id ALTER COLUMN tariff_service_type_id SET DEFAULT nextval('tid_tariff_service_type_id_tariff_service_type_id_seq'::regclass);


--
-- TOC entry 2595 (class 2604 OID 19402)
-- Name: tid_tariff_type_id tariff_type_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_tariff_type_id ALTER COLUMN tariff_type_id SET DEFAULT nextval('tid_tariff_type_id_tariff_type_id_seq'::regclass);


--
-- TOC entry 2596 (class 2604 OID 19403)
-- Name: tid_user_id user_id; Type: DEFAULT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_user_id ALTER COLUMN user_id SET DEFAULT nextval('tid_user_id_user_id_seq'::regclass);


--
-- TOC entry 3054 (class 0 OID 18998)
-- Dependencies: 187
-- Data for Name: it_user; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY it_user (user_id, user_code, user_name, password, profile_id, start_date, end_date, status_id, input_date, input_user, modif_date, modif_user) FROM stdin;
2	DBADMIN	Base DBA Admin	e4a7bd7e5ace5690baab7972a9455efc	2	2016-06-30 00:00:00	9999-12-31 23:59:59	1	2016-06-30 20:04:17	SYSTEM	\N	\N
3	USER	Base User	2e40ad879e955201df4dedbf8d479a12	3	2016-06-30 00:00:00	9999-12-31 23:59:59	1	2016-06-30 20:04:48	SYSTEM	\N	\N
4	USER1	User N1	9f693771ca12c43759045cdf4295e9f5	3	2016-06-30 00:00:00	9999-12-31 23:59:59	1	2016-06-30 20:05:26	SYSTEM	\N	\N
13	3	3	eccbc87e4b5ce2fe28308fd9f2a7baf3	3	1900-01-01 00:00:00	9999-12-31 23:59:59	1	2016-10-29 16:07:54.211	USER	\N	\N
7	1	1	c4ca4238a0b923820dcc509a6f75849b	1	1900-01-01 00:00:00	9999-12-31 23:59:59	1	2016-10-22 12:35:23.268	USER	\N	\N
5	TEST	TEST USER	033bd94b1168d7e4f0d644c3c95e35bf	3	1900-01-01 00:00:00	2017-04-30 23:59:59	1	2016-09-29 19:21:04.832	USER	2017-05-27 12:29:57.136	USER
5	TEST	TEST USER	033bd94b1168d7e4f0d644c3c95e35bf	3	2017-05-01 00:00:00	9999-12-31 23:59:59	1	2017-05-27 12:29:57.136	USER	\N	\N
9	2	2	b6d767d2f8ed5d21a44b0e5886680cb9	3	1900-01-01 00:00:00	2020-07-31 23:59:59	1	2016-10-29 12:24:40.888	USER	2017-05-30 21:13:28.935	USER
9	2	2	b6d767d2f8ed5d21a44b0e5886680cb9	3	2020-08-01 00:00:00	9999-12-31 23:59:59	1	2017-05-30 21:13:28.935	USER	2017-05-30 21:14:10.673	USER
114	ADMIN	ADMIN	73acd9a5972130b75066c82595a1fae3	1	1900-01-01 00:00:00	9999-12-31 23:59:59	1	2017-07-12 18:04:29	USER	\N	\N
\.


--
-- TOC entry 3055 (class 0 OID 19004)
-- Dependencies: 188
-- Data for Name: mt_application_level; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_application_level (application_level_id, application_level_code, description) FROM stdin;
3	PROD	Product level
9	SERV	Service level
10	EQUI	Equipment level
14	PROM	Promotion level
\.


--
-- TOC entry 3730 (class 0 OID 0)
-- Dependencies: 189
-- Name: mt_application_level_application_level_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_application_level_application_level_id_seq', 16, true);


--
-- TOC entry 3057 (class 0 OID 19009)
-- Dependencies: 190
-- Data for Name: mt_application_unit; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_application_unit (application_unit_id, application_unit_code, description) FROM stdin;
3	EUR	Discount in euros
2	UNIT	Discount in units
\.


--
-- TOC entry 3731 (class 0 OID 0)
-- Dependencies: 191
-- Name: mt_application_unit_application_unit_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_application_unit_application_unit_id_seq', 3, true);


--
-- TOC entry 3059 (class 0 OID 19014)
-- Dependencies: 192
-- Data for Name: mt_business_scope; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_business_scope (business_scope_id, business_scope_code, description) FROM stdin;
1	CORP	Corporate scope
2	PERS	Personal scope
3	IND	Indiferent scope
\.


--
-- TOC entry 3732 (class 0 OID 0)
-- Dependencies: 193
-- Name: mt_business_scope_business_scope_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_business_scope_business_scope_id_seq', 3, true);


--
-- TOC entry 3061 (class 0 OID 19019)
-- Dependencies: 194
-- Data for Name: mt_consumption_code; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_consumption_code (consumption_code_id, consumption_code, description, start_date, end_date, status_id, consumption_type_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3062 (class 0 OID 19025)
-- Dependencies: 195
-- Data for Name: mt_consumption_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_consumption_type (consumption_type_id, consumption_type_code, description) FROM stdin;
2	FCALL	Fixed phone call
3	MCALL	Mobile phone call
\.


--
-- TOC entry 3733 (class 0 OID 0)
-- Dependencies: 196
-- Name: mt_consumption_type_consumption_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_consumption_type_consumption_type_id_seq', 3, true);


--
-- TOC entry 3064 (class 0 OID 19030)
-- Dependencies: 197
-- Data for Name: mt_discount_concept; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_discount_concept (discount_concept_id, discount_concept_code, description) FROM stdin;
1	FEE	Fee discount
3	FPH	Fixed phone discount
4	MOB	Mobile phone discount
\.


--
-- TOC entry 3734 (class 0 OID 0)
-- Dependencies: 198
-- Name: mt_discount_concept_discount_concept_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_discount_concept_discount_concept_id_seq', 4, true);


--
-- TOC entry 3066 (class 0 OID 19035)
-- Dependencies: 199
-- Data for Name: mt_discount_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_discount_type (discount_type_id, discount_type_code, description) FROM stdin;
1	FIXED	Fixed discount
2	VARPER	Variable percentaje discount
8	VARAMO	Variable amount discount
14	TRES	TRES
\.


--
-- TOC entry 3735 (class 0 OID 0)
-- Dependencies: 200
-- Name: mt_discount_type_discount_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_discount_type_discount_type_id_seq', 14, true);


--
-- TOC entry 3068 (class 0 OID 19040)
-- Dependencies: 201
-- Data for Name: mt_entity_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_entity_type (entity_type_id, entity_type_code, description) FROM stdin;
2	PROD	Product entity
1	USER	User entity
4	EQUIP	Equipment entity
3	SERV	Service entity
5	PROM	Promotion entity
6	CONS	Consumption entity
\.


--
-- TOC entry 3736 (class 0 OID 0)
-- Dependencies: 202
-- Name: mt_entity_type_entity_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_entity_type_entity_type_id_seq', 6, true);


--
-- TOC entry 3070 (class 0 OID 19045)
-- Dependencies: 203
-- Data for Name: mt_equipment_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_equipment_type (equipment_type_id, equipment_code, description, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3071 (class 0 OID 19051)
-- Dependencies: 204
-- Data for Name: mt_fee_code; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_fee_code (fee_code_id, fee_code, description, start_date, end_date, status_id, price, overwrite, application_level_id, recurrence_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3072 (class 0 OID 19059)
-- Dependencies: 205
-- Data for Name: mt_plan_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_plan_type (plan_type_id, plan_code, description, start_date, end_date, status_id, application_level_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3073 (class 0 OID 19065)
-- Dependencies: 206
-- Data for Name: mt_product_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_product_type (product_type_id, product_type_code, description, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user, entity_type_id) FROM stdin;
4	PROD 04	Fourth product type	1900-01-01 00:00:00	9999-12-31 23:59:59	3	1	1	2017-09-06 20:48:31.876	ADMIN	\N	\N	2
2	PROD 02	Second product type	1900-01-01 00:00:00	9999-12-31 23:59:59	3	2	2	2017-08-31 13:22:59	ADMIN	2017-09-06 20:49:44	ADMIN	2
3	PROD 03	Third product type	1900-01-01 00:00:00	9999-12-31 23:59:59	3	1	1	2017-09-03 13:49:21	ADMIN	2017-09-06 20:50:40	ADMIN	2
1	PROD 01	First product of the catalog	1900-01-01 00:00:00	1999-12-31 23:59:59	3	3	1	2017-08-24 19:03:17	ADMIN	2017-10-24 20:32:37	ADMIN	2
1	PROD 1	First product of the catalog	2000-01-01 00:00:00	9999-12-31 23:59:59	3	3	1	2017-09-07 21:16:54	ADMIN	2017-09-06 21:24:05	ADMIN	2
\.


--
-- TOC entry 3074 (class 0 OID 19072)
-- Dependencies: 207
-- Data for Name: mt_profile; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_profile (profile_id, profile_code, description, start_date, end_date, input_date, input_user, modif_date, modif_user) FROM stdin;
1	ADMIN	application admin	2016-06-30 19:20:48	9999-12-31 23:59:59	2016-06-30 19:20:48	SYSTEM	\N	\N
2	DBADMIN	db admin	2016-06-30 19:21:14	9999-12-31 23:59:59	2016-06-30 19:21:14	SYSTEM	\N	\N
3	USER	user	2016-06-30 19:21:32	9999-12-31 23:59:59	2016-06-30 19:21:32	SYSTEM	\N	\N
\.


--
-- TOC entry 3075 (class 0 OID 19078)
-- Dependencies: 208
-- Data for Name: mt_promotion_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_promotion_type (promotion_type_id, promotion_code, voucher, description, start_date, end_date, status_id, application_level_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3076 (class 0 OID 19085)
-- Dependencies: 209
-- Data for Name: mt_recurrence; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_recurrence (recurrence_id, recurrence_code, description) FROM stdin;
1	ONEOFF	One-off
2	INPUT	Input
3	MONTH	Monthly
4	ANNUAL	Annual
\.


--
-- TOC entry 3737 (class 0 OID 0)
-- Dependencies: 210
-- Name: mt_recurrence_recurrence_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_recurrence_recurrence_id_seq', 4, true);


--
-- TOC entry 3078 (class 0 OID 19090)
-- Dependencies: 211
-- Data for Name: mt_service_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_service_type (service_type_id, service_type_code, description, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user, entity_type_id) FROM stdin;
1	SERV 01	First service type	1900-01-01 00:00:00	9999-12-31 23:59:59	7	1	1	2017-08-31 13:21:02	ADMIN	2017-09-06 22:41:15	ADMIN	3
2	SERV 02	Second servyce type	1900-01-01 00:00:00	9999-12-31 23:59:59	7	2	2	2017-08-31 13:22:07	ADMIN	2017-09-06 22:41:38	ADMIN	3
3	SERV 03	Third service type	1900-01-01 00:00:00	9999-12-31 23:59:59	7	2	2	2017-08-31 13:34:27	ADMIN	2017-09-06 22:42:03	ADMIN	3
\.


--
-- TOC entry 3079 (class 0 OID 19097)
-- Dependencies: 212
-- Data for Name: mt_status; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_status (status_id, entity_type_id, status_code, description) FROM stdin;
1	1	ACTIVE	user active
2	1	CANCEL	user cancelled
7	3	ACTIVE	Active service type status
8	3	CANCEL	Cancel service type status
9	3	DEBTOR	Debtor service type status
10	3	SUSP	Suspended service type status
3	2	ACTIVE	Active product type status
4	2	CANCEL	Cancel product type status
6	2	DEBTOR	Debtor product type status
5	2	SUSP	Suspended product type status
\.


--
-- TOC entry 3738 (class 0 OID 0)
-- Dependencies: 213
-- Name: mt_status_status_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_status_status_id_seq', 10, true);


--
-- TOC entry 3081 (class 0 OID 19102)
-- Dependencies: 214
-- Data for Name: mt_tariff_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_tariff_type (tariff_type_id, tariff_code, prorrate, description, start_date, end_date, status_id, application_level_id, recurrence_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3082 (class 0 OID 19109)
-- Dependencies: 215
-- Data for Name: mt_technology_scope; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY mt_technology_scope (technology_scope_id, technology_scope_code, description) FROM stdin;
1	FIX	Fixed phone
2	MOB	Mobile phone
3	BTV	Basic television
\.


--
-- TOC entry 3739 (class 0 OID 0)
-- Dependencies: 216
-- Name: mt_technology_scope_technology_scope_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_technology_scope_technology_scope_id_seq', 3, true);


--
-- TOC entry 3084 (class 0 OID 19114)
-- Dependencies: 217
-- Data for Name: rmt_fee_equip_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_fee_equip_type (fee_equipment_type_id, fee_code_id, equipment_type_id, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3085 (class 0 OID 19120)
-- Dependencies: 218
-- Data for Name: rmt_fee_product_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_fee_product_type (fee_product_type_id, fee_code_id, product_type_id, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3086 (class 0 OID 19126)
-- Dependencies: 219
-- Data for Name: rmt_fee_promotion_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_fee_promotion_type (fee_promotion_type_id, fee_code_id, promotion_type_id, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3087 (class 0 OID 19132)
-- Dependencies: 220
-- Data for Name: rmt_fee_service_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_fee_service_type (fee_service_type_id, fee_code_id, service_type_id, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3088 (class 0 OID 19138)
-- Dependencies: 221
-- Data for Name: rmt_plan_charge_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_plan_charge_type (plan_charge_type_id, plan_type_id, charge_type, charge_type_id, start_date, end_date, status_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3089 (class 0 OID 19144)
-- Dependencies: 222
-- Data for Name: rmt_plan_discount_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_plan_discount_type (plan_discount_type_id, plan_type_id, start_date, end_date, status_id, percentaje, threshold_unit_id, min_threshold, max_threshold, discount_unit_id, max_discount, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3090 (class 0 OID 19155)
-- Dependencies: 223
-- Data for Name: rmt_plan_prerreq_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_plan_prerreq_type (plan_prerreq_type_id, plan_type_id, prerrequisite_type, prerrequisite_type_id, start_date, end_date, status_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3091 (class 0 OID 19161)
-- Dependencies: 224
-- Data for Name: rmt_plan_promotion_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_plan_promotion_type (plan_promotion_type_id, promotion_type_id, promotion_code, plan_type_id, plan_code, start_date, end_date, status_id, discount_type_id, discount_concept_id, prorrate, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3092 (class 0 OID 19168)
-- Dependencies: 225
-- Data for Name: rmt_promotion_equipment_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_promotion_equipment_type (prom_equip_type_id, promotion_type_id, equipment_type_id, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3093 (class 0 OID 19174)
-- Dependencies: 226
-- Data for Name: rmt_promotion_product_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_promotion_product_type (prom_prod_type_id, promotion_type_id, product_type_id, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3094 (class 0 OID 19180)
-- Dependencies: 227
-- Data for Name: rmt_promotion_service_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_promotion_service_type (prom_serv_type_id, promotion_type_id, service_type_id, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3095 (class 0 OID 19186)
-- Dependencies: 228
-- Data for Name: rmt_service_equipment_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_service_equipment_type (service_equipment_type_id, service_type_id, equipment_type_id, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3096 (class 0 OID 19192)
-- Dependencies: 229
-- Data for Name: rmt_tariff_equipment_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_tariff_equipment_type (tariff_equipment_type_id, tariff_type_id, equipment_type_id, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3097 (class 0 OID 19198)
-- Dependencies: 230
-- Data for Name: rmt_tariff_product_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_tariff_product_type (tariff_product_type_id, tariff_type_id, product_type_id, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3098 (class 0 OID 19204)
-- Dependencies: 231
-- Data for Name: rmt_tariff_promotion_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_tariff_promotion_type (tariff_promotion_type_id, tariff_type_id, promotion_type_id, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3099 (class 0 OID 19210)
-- Dependencies: 232
-- Data for Name: rmt_tariff_service_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY rmt_tariff_service_type (tariff_service_type_id, tariff_type_id, service_type_id, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user) FROM stdin;
\.


--
-- TOC entry 3100 (class 0 OID 19216)
-- Dependencies: 233
-- Data for Name: test_menu; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) FROM stdin;
1	VIEW	S	0	1	View Data (for users)	USER	\N	1	\N
3	VIEW	S	0	1	View Data (for appl_admin)	ADMIN	\N	1	\N
5	VIEW	S	0	1	View Data (for dba_admin)	DBADMIN	\N	1	\N
60	Consumption Code	I	1	6	Consumption Code	ADMIN	54	1	consumption_code.xhtml
67	Consumption Code	I	1	6	Consumption Code	USER	61	1	consumption_code.xhtml
23	Create an user	I	1	2	Create an user for the application (for dba admin)	DBADMIN	6	1	create_user.xhtml
25	PROMOTIONS	S	1	1	Promotions	ADMIN	24	1	\N
18	Application admin	I	2	1	List of all application admin (for dba admin)	DBADMIN	17	1	list_of_app_admin.xhtml
12	Application admin	I	2	1	List of all application admin (for appl_admin)	ADMIN	11	1	list_of_app_admin.xhtml
19	DBA admin	I	2	2	List of all dba admin (for dba admin)	DBADMIN	17	1	list_of_dba_admin.xhtml
2	MODIFY	S	0	2	Modify Data (for users)	USER	\N	1	\N
4	MODIFY	S	0	2	Modify Data (for appl_admin)	ADMIN	\N	1	\N
6	MODIFY	S	0	2	Modify Data (for dba_admin)	DBADMIN	\N	1	\N
15	Other password	I	2	2	Change the password for other user than actual user (for appl_admin)	ADMIN	13	1	change_other_password.xhtlm
22	Other password	I	2	2	Change the password for other user than actual user (for dba admin)	DBADMIN	20	1	change_other_password.xhtlm
7	List of users	I	1	1	All user list in the application (for users)	USER	1	1	list_of_users.xhtml
13	Change password	S	1	1	Change the password for an user (for appl_admin)	ADMIN	4	1	\N
11	List of admin	S	1	2	All admin list in the application (for appl_admin)	ADMIN	3	1	\N
17	List of admin	S	1	2	All admin list in the application (for dba_admin)	DBADMIN	5	1	\N
24	PARAMETRICS	S	0	3	Parametrics data	ADMIN	\N	1	\N
20	Change password	S	1	1	6Change the password for an user (for dba admin)	DBADMIN	6	1	\N
8	Change password	S	1	1	Change the password for an user (for users)	USER	2	1	\N
26	Application Level	I	2	1	Application level to the promotion	ADMIN	25	1	application_level.xhtml
9	My password	I	2	1	Change the password for the actual user (users)	USER	8	1	change_my_password.xhtml
14	My password	I	2	1	Change the password for the actual user (for appl_admin)	ADMIN	13	1	change_my_password.xhtml
21	My password	I	2	1	Change the password for the actual user (for dba admin)	DBADMIN	20	1	change_my_password.xhtml
10	List of users	I	1	1	All user list in the application (for appl_admin)	ADMIN	3	1	list_of_users.xhtml
16	List of users	I	1	1	All user list in the application (for dba_admin)	DBADMIN	5	1	list_of_users.xhtml
27	Application Unit	I	2	2	Application unit to the promotion	ADMIN	25	1	application_unit.xhtml
61	CATALOG	S	0	5	Catalog	USER	\N	1	\N
62	Product Type	I	1	1	Product Type	USER	61	1	product_type.xhtml
66	Promotion Plan Type	I	1	5	Promotion Plan Type	USER	61	1	promotion_plan_type.xhtml
65	Promotion Type	I	1	4	Promotion Type	USER	61	1	promotion_type.xhtml
64	Equipment Type	I	1	3	Equipment Type	USER	61	1	equipment_type.xhtml
63	Service Type	I	1	2	Service Type	USER	61	1	service_type.xhtml
31	Consumption Types	I	2	1	Consumption types	ADMIN	30	1	consumption_type.xhtml
29	Discount Concepts	I	2	4	Discount concept for the promotion	ADMIN	25	1	discount_concept.xhtml
30	CONSUMPTIONS	S	1	1	Consumptions	ADMIN	24	1	\N
32	OTHERS	S	1	1	Others	ADMIN	24	1	\N
33	Business Scope	I	2	1	Bussines scope	ADMIN	32	1	business_scope.xhtml
34	Recurrence	I	2	2	Recurrence of the charges	ADMIN	32	1	recurrence.xhtml
36	PARAMETRICS	S	0	3	Parametrics data	USER	\N	1	\N
48	ENTITIES	S	0	4	Entities	ADMIN	\N	1	\N
49	Entity Types	I	1	1	Entity types	ADMIN	48	1	entity_type.xhtml
50	Status	I	1	2	Status of the entities	ADMIN	48	1	status.xhtml
51	ENTITIES	S	0	4	Entities	USER	\N	1	\N
52	Entity Types	I	1	1	Entity types	USER	51	1	entity_type.xhtml
53	Status	I	1	2	Status of the entities	USER	51	1	status.xhtml
54	CATALOG	S	0	5	Catalog	ADMIN	\N	1	\N
55	Product Type	I	1	1	Product Type	ADMIN	54	1	product_type.xhtml
56	Service Type	I	1	2	Service Type	ADMIN	54	1	service_type.xhtml
57	Equipment Type	I	1	3	Equipment Type	ADMIN	54	1	equipment_type.xhtml
58	Promotion Type	I	1	4	Promotion Type	ADMIN	54	1	promotion_type.xhtml
59	Promotion Plan Type	I	1	5	Promotion Plan Type	ADMIN	54	1	promotion_plan_type.xhtml
37	PROMOTIONS	S	1	1	Promotions	USER	36	1	\N
38	Application Levels	I	2	1	Application level to the promotion	USER	37	1	application_level.xhtml
39	Application Units	I	2	2	Application unit to the promotion	USER	37	1	application_unit.xhtml
41	Discount Concepts	I	2	4	Discount concept for the promotion	USER	37	1	discount_concept.xhtml
43	Consumption Types	I	2	1	Consumption types	USER	42	1	consumption_type.xhtml
35	Technology Scope	I	2	3	Technology scope of the entities	ADMIN	32	1	technology_scope.xhtml
47	Technology Scope	I	2	3	Technology scope of the entities	USER	44	1	technology_scope.xhtml
42	CONSUMPTIONS	S	1	1	Consumptions	USER	36	1	\N
45	Business Scope	I	2	1	Bussines scope	USER	44	1	business_scope.xhtml
46	Recurrence	I	2	2	Recurrence of the charges	USER	44	1	recurrence.xhtml
44	OTHERS	S	1	1	Others	USER	36	1	\N
28	Discount Types	I	2	3	Discount type for the promotion	ADMIN	25	1	discount_type.xhtml
40	Discount Types	I	2	3	Discount type for the promotion	USER	37	1	discount_type.xhtml
\.


--
-- TOC entry 3101 (class 0 OID 19219)
-- Dependencies: 234
-- Data for Name: test_user; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY test_user (user_id, user_code, profile_id, profile_code, password) FROM stdin;
1	billing_admin	1	DBA_ADMIN	b412519dfd76f8ccd682488239e31577
4	user	3	USER	ee11cbb19052e40b07aac0ca060c23ee
2	billing_appl	2	APPL_ADMIN	15b401794cf74c38e1b7c89508eec939
3	billing_appl_2	2	APPL_ADMIN	15b401794cf74c38e1b7c89508eec939
\.


--
-- TOC entry 3102 (class 0 OID 19222)
-- Dependencies: 235
-- Data for Name: tid_consumption_code_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_consumption_code_id (consumption_code_id) FROM stdin;
\.


--
-- TOC entry 3740 (class 0 OID 0)
-- Dependencies: 236
-- Name: tid_consumption_code_id_consumption_code_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_consumption_code_id_consumption_code_id_seq', 1, false);


--
-- TOC entry 3104 (class 0 OID 19227)
-- Dependencies: 237
-- Data for Name: tid_equipment_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_equipment_type_id (equipment_type_id) FROM stdin;
\.


--
-- TOC entry 3741 (class 0 OID 0)
-- Dependencies: 238
-- Name: tid_equipment_type_id_equipment_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_equipment_type_id_equipment_type_id_seq', 1, false);


--
-- TOC entry 3106 (class 0 OID 19232)
-- Dependencies: 239
-- Data for Name: tid_fee_code_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_fee_code_id (fee_code_id) FROM stdin;
\.


--
-- TOC entry 3742 (class 0 OID 0)
-- Dependencies: 240
-- Name: tid_fee_code_id_fee_code_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_fee_code_id_fee_code_id_seq', 1, false);


--
-- TOC entry 3108 (class 0 OID 19237)
-- Dependencies: 241
-- Data for Name: tid_fee_equipment_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_fee_equipment_type_id (fee_equipment_type_id) FROM stdin;
\.


--
-- TOC entry 3743 (class 0 OID 0)
-- Dependencies: 242
-- Name: tid_fee_equipment_type_id_fee_equipment_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_fee_equipment_type_id_fee_equipment_type_id_seq', 1, false);


--
-- TOC entry 3110 (class 0 OID 19242)
-- Dependencies: 243
-- Data for Name: tid_fee_product_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_fee_product_type_id (fee_product_type_id) FROM stdin;
\.


--
-- TOC entry 3744 (class 0 OID 0)
-- Dependencies: 244
-- Name: tid_fee_product_type_id_fee_product_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_fee_product_type_id_fee_product_type_id_seq', 1, false);


--
-- TOC entry 3112 (class 0 OID 19247)
-- Dependencies: 245
-- Data for Name: tid_fee_promotion_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_fee_promotion_type_id (fee_promotion_type_id) FROM stdin;
\.


--
-- TOC entry 3745 (class 0 OID 0)
-- Dependencies: 246
-- Name: tid_fee_promotion_type_id_fee_promotion_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_fee_promotion_type_id_fee_promotion_type_id_seq', 1, false);


--
-- TOC entry 3114 (class 0 OID 19252)
-- Dependencies: 247
-- Data for Name: tid_fee_service_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_fee_service_type_id (fee_service_type_id) FROM stdin;
\.


--
-- TOC entry 3746 (class 0 OID 0)
-- Dependencies: 248
-- Name: tid_fee_service_type_id_fee_service_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_fee_service_type_id_fee_service_type_id_seq', 1, false);


--
-- TOC entry 3116 (class 0 OID 19257)
-- Dependencies: 249
-- Data for Name: tid_plan_charge_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_plan_charge_type_id (plan_charge_type_id) FROM stdin;
\.


--
-- TOC entry 3747 (class 0 OID 0)
-- Dependencies: 250
-- Name: tid_plan_charge_type_id_plan_charge_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_plan_charge_type_id_plan_charge_type_id_seq', 1, false);


--
-- TOC entry 3118 (class 0 OID 19262)
-- Dependencies: 251
-- Data for Name: tid_plan_discount_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_plan_discount_type_id (plan_discount_type_id) FROM stdin;
\.


--
-- TOC entry 3748 (class 0 OID 0)
-- Dependencies: 252
-- Name: tid_plan_discount_type_id_plan_discount_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_plan_discount_type_id_plan_discount_type_id_seq', 1, false);


--
-- TOC entry 3120 (class 0 OID 19267)
-- Dependencies: 253
-- Data for Name: tid_plan_prerreq_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_plan_prerreq_type_id (plan_prerreq_type_id) FROM stdin;
\.


--
-- TOC entry 3749 (class 0 OID 0)
-- Dependencies: 254
-- Name: tid_plan_prerreq_type_id_plan_prerreq_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_plan_prerreq_type_id_plan_prerreq_type_id_seq', 1, false);


--
-- TOC entry 3122 (class 0 OID 19272)
-- Dependencies: 255
-- Data for Name: tid_plan_promotion_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_plan_promotion_type_id (plan_promotion_type_id) FROM stdin;
\.


--
-- TOC entry 3750 (class 0 OID 0)
-- Dependencies: 256
-- Name: tid_plan_promotion_type_id_plan_promotion_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_plan_promotion_type_id_plan_promotion_type_id_seq', 1, false);


--
-- TOC entry 3124 (class 0 OID 19277)
-- Dependencies: 257
-- Data for Name: tid_plan_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_plan_type_id (plan_type_id) FROM stdin;
\.


--
-- TOC entry 3751 (class 0 OID 0)
-- Dependencies: 258
-- Name: tid_plan_type_id_plan_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_plan_type_id_plan_type_id_seq', 1, false);


--
-- TOC entry 3126 (class 0 OID 19282)
-- Dependencies: 259
-- Data for Name: tid_product_service_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_product_service_type_id (product_service_type_id) FROM stdin;
\.


--
-- TOC entry 3752 (class 0 OID 0)
-- Dependencies: 260
-- Name: tid_product_service_type_id_product_service_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_product_service_type_id_product_service_type_id_seq', 1, false);


--
-- TOC entry 3128 (class 0 OID 19287)
-- Dependencies: 261
-- Data for Name: tid_product_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_product_type_id (product_type_id) FROM stdin;
1
2
3
4
\.


--
-- TOC entry 3753 (class 0 OID 0)
-- Dependencies: 262
-- Name: tid_product_type_id_product_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_product_type_id_product_type_id_seq', 4, true);


--
-- TOC entry 3130 (class 0 OID 19292)
-- Dependencies: 263
-- Data for Name: tid_profile_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_profile_id (profile_id) FROM stdin;
1
2
3
\.


--
-- TOC entry 3754 (class 0 OID 0)
-- Dependencies: 264
-- Name: tid_profile_id_profile_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_profile_id_profile_id_seq', 1, false);


--
-- TOC entry 3132 (class 0 OID 19297)
-- Dependencies: 265
-- Data for Name: tid_promotion_equipment_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_promotion_equipment_type_id (prom_equip_type_id) FROM stdin;
\.


--
-- TOC entry 3755 (class 0 OID 0)
-- Dependencies: 266
-- Name: tid_promotion_equipment_type_id_prom_equip_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_promotion_equipment_type_id_prom_equip_type_id_seq', 1, false);


--
-- TOC entry 3134 (class 0 OID 19302)
-- Dependencies: 267
-- Data for Name: tid_promotion_product_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_promotion_product_type_id (prom_prod_type_id) FROM stdin;
\.


--
-- TOC entry 3756 (class 0 OID 0)
-- Dependencies: 268
-- Name: tid_promotion_product_type_id_prom_prod_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_promotion_product_type_id_prom_prod_type_id_seq', 1, false);


--
-- TOC entry 3136 (class 0 OID 19307)
-- Dependencies: 269
-- Data for Name: tid_promotion_promotion_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_promotion_promotion_type_id (prom_prom_type_id) FROM stdin;
\.


--
-- TOC entry 3757 (class 0 OID 0)
-- Dependencies: 270
-- Name: tid_promotion_promotion_type_id_prom_prom_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_promotion_promotion_type_id_prom_prom_type_id_seq', 1, false);


--
-- TOC entry 3138 (class 0 OID 19312)
-- Dependencies: 271
-- Data for Name: tid_promotion_service_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_promotion_service_type_id (prom_serv_type_id) FROM stdin;
\.


--
-- TOC entry 3758 (class 0 OID 0)
-- Dependencies: 272
-- Name: tid_promotion_service_type_id_prom_serv_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_promotion_service_type_id_prom_serv_type_id_seq', 1, false);


--
-- TOC entry 3140 (class 0 OID 19317)
-- Dependencies: 273
-- Data for Name: tid_promotion_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_promotion_type_id (promotion_type_id) FROM stdin;
\.


--
-- TOC entry 3759 (class 0 OID 0)
-- Dependencies: 274
-- Name: tid_promotion_type_id_promotion_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_promotion_type_id_promotion_type_id_seq', 1, false);


--
-- TOC entry 3142 (class 0 OID 19322)
-- Dependencies: 275
-- Data for Name: tid_service_equipment_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_service_equipment_type_id (service_equipment_type_id) FROM stdin;
\.


--
-- TOC entry 3760 (class 0 OID 0)
-- Dependencies: 276
-- Name: tid_service_equipment_type_id_service_equipment_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_service_equipment_type_id_service_equipment_type_id_seq', 1, false);


--
-- TOC entry 3144 (class 0 OID 19327)
-- Dependencies: 277
-- Data for Name: tid_service_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_service_type_id (service_type_id) FROM stdin;
1
2
3
\.


--
-- TOC entry 3761 (class 0 OID 0)
-- Dependencies: 278
-- Name: tid_service_type_id_service_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_service_type_id_service_type_id_seq', 3, true);


--
-- TOC entry 3146 (class 0 OID 19332)
-- Dependencies: 279
-- Data for Name: tid_tariff_equipment_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_tariff_equipment_type_id (tariff_equipment_type_id) FROM stdin;
\.


--
-- TOC entry 3762 (class 0 OID 0)
-- Dependencies: 280
-- Name: tid_tariff_equipment_type_id_tariff_equipment_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_tariff_equipment_type_id_tariff_equipment_type_id_seq', 1, false);


--
-- TOC entry 3148 (class 0 OID 19337)
-- Dependencies: 281
-- Data for Name: tid_tariff_product_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_tariff_product_type_id (tariff_product_type_id) FROM stdin;
\.


--
-- TOC entry 3763 (class 0 OID 0)
-- Dependencies: 282
-- Name: tid_tariff_product_type_id_tariff_product_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_tariff_product_type_id_tariff_product_type_id_seq', 1, false);


--
-- TOC entry 3150 (class 0 OID 19342)
-- Dependencies: 283
-- Data for Name: tid_tariff_promotion_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_tariff_promotion_type_id (tariff_promotion_type_id) FROM stdin;
\.


--
-- TOC entry 3764 (class 0 OID 0)
-- Dependencies: 284
-- Name: tid_tariff_promotion_type_id_tariff_promotion_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_tariff_promotion_type_id_tariff_promotion_type_id_seq', 1, false);


--
-- TOC entry 3152 (class 0 OID 19347)
-- Dependencies: 285
-- Data for Name: tid_tariff_service_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_tariff_service_type_id (tariff_service_type_id) FROM stdin;
\.


--
-- TOC entry 3765 (class 0 OID 0)
-- Dependencies: 286
-- Name: tid_tariff_service_type_id_tariff_service_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_tariff_service_type_id_tariff_service_type_id_seq', 1, false);


--
-- TOC entry 3154 (class 0 OID 19352)
-- Dependencies: 287
-- Data for Name: tid_tariff_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_tariff_type_id (tariff_type_id) FROM stdin;
\.


--
-- TOC entry 3766 (class 0 OID 0)
-- Dependencies: 288
-- Name: tid_tariff_type_id_tariff_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_tariff_type_id_tariff_type_id_seq', 1, false);


--
-- TOC entry 3156 (class 0 OID 19357)
-- Dependencies: 289
-- Data for Name: tid_user_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

COPY tid_user_id (user_id) FROM stdin;
1
2
3
4
5
9
13
7
112
114
115
116
\.


--
-- TOC entry 3767 (class 0 OID 0)
-- Dependencies: 290
-- Name: tid_user_id_user_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_user_id_user_id_seq', 116, true);


--
-- TOC entry 2598 (class 2606 OID 19405)
-- Name: it_user pk_it_user; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY it_user
    ADD CONSTRAINT pk_it_user PRIMARY KEY (user_id, start_date);


--
-- TOC entry 2604 (class 2606 OID 19407)
-- Name: mt_application_level pk_mt_application_level; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_application_level
    ADD CONSTRAINT pk_mt_application_level PRIMARY KEY (application_level_id);


--
-- TOC entry 2608 (class 2606 OID 19409)
-- Name: mt_application_unit pk_mt_application_unit; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_application_unit
    ADD CONSTRAINT pk_mt_application_unit PRIMARY KEY (application_unit_id);


--
-- TOC entry 2612 (class 2606 OID 19411)
-- Name: mt_business_scope pk_mt_business_scope; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_business_scope
    ADD CONSTRAINT pk_mt_business_scope PRIMARY KEY (business_scope_id);


--
-- TOC entry 2616 (class 2606 OID 19413)
-- Name: mt_consumption_code pk_mt_consumption_code; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_consumption_code
    ADD CONSTRAINT pk_mt_consumption_code PRIMARY KEY (consumption_code_id, start_date);


--
-- TOC entry 2618 (class 2606 OID 19415)
-- Name: mt_consumption_type pk_mt_consumption_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_consumption_type
    ADD CONSTRAINT pk_mt_consumption_type PRIMARY KEY (consumption_type_id);


--
-- TOC entry 2622 (class 2606 OID 19417)
-- Name: mt_discount_concept pk_mt_discount_concept; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_discount_concept
    ADD CONSTRAINT pk_mt_discount_concept PRIMARY KEY (discount_concept_id);


--
-- TOC entry 2626 (class 2606 OID 19419)
-- Name: mt_discount_type pk_mt_discount_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_discount_type
    ADD CONSTRAINT pk_mt_discount_type PRIMARY KEY (discount_type_id);


--
-- TOC entry 2630 (class 2606 OID 19421)
-- Name: mt_entity_type pk_mt_entity_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_entity_type
    ADD CONSTRAINT pk_mt_entity_type PRIMARY KEY (entity_type_id);


--
-- TOC entry 2634 (class 2606 OID 19423)
-- Name: mt_equipment_type pk_mt_equipment_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_equipment_type
    ADD CONSTRAINT pk_mt_equipment_type PRIMARY KEY (equipment_type_id, start_date);


--
-- TOC entry 2636 (class 2606 OID 19425)
-- Name: mt_fee_code pk_mt_fee_code; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_fee_code
    ADD CONSTRAINT pk_mt_fee_code PRIMARY KEY (fee_code_id, start_date);


--
-- TOC entry 2638 (class 2606 OID 19427)
-- Name: mt_plan_type pk_mt_plan_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_plan_type
    ADD CONSTRAINT pk_mt_plan_type PRIMARY KEY (plan_type_id, start_date);


--
-- TOC entry 2640 (class 2606 OID 19429)
-- Name: mt_product_type pk_mt_product_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_product_type
    ADD CONSTRAINT pk_mt_product_type PRIMARY KEY (product_type_id, start_date);


--
-- TOC entry 2646 (class 2606 OID 19431)
-- Name: mt_profile pk_mt_profile; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_profile
    ADD CONSTRAINT pk_mt_profile PRIMARY KEY (profile_id, start_date);


--
-- TOC entry 2648 (class 2606 OID 19433)
-- Name: mt_promotion_type pk_mt_promotion_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_promotion_type
    ADD CONSTRAINT pk_mt_promotion_type PRIMARY KEY (promotion_type_id, start_date);


--
-- TOC entry 2650 (class 2606 OID 19435)
-- Name: mt_recurrence pk_mt_recurrence; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_recurrence
    ADD CONSTRAINT pk_mt_recurrence PRIMARY KEY (recurrence_id);


--
-- TOC entry 2654 (class 2606 OID 19437)
-- Name: mt_service_type pk_mt_service_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_service_type
    ADD CONSTRAINT pk_mt_service_type PRIMARY KEY (service_type_id, start_date);


--
-- TOC entry 2660 (class 2606 OID 19439)
-- Name: mt_status pk_mt_status; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_status
    ADD CONSTRAINT pk_mt_status PRIMARY KEY (status_id);


--
-- TOC entry 2664 (class 2606 OID 19441)
-- Name: mt_tariff_type pk_mt_tariff_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_tariff_type
    ADD CONSTRAINT pk_mt_tariff_type PRIMARY KEY (tariff_type_id, start_date);


--
-- TOC entry 2666 (class 2606 OID 19443)
-- Name: mt_technology_scope pk_mt_technology_scope; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_technology_scope
    ADD CONSTRAINT pk_mt_technology_scope PRIMARY KEY (technology_scope_id);


--
-- TOC entry 2670 (class 2606 OID 19445)
-- Name: rmt_fee_equip_type pk_rmt_fee_equip_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_equip_type
    ADD CONSTRAINT pk_rmt_fee_equip_type PRIMARY KEY (fee_equipment_type_id, start_date);


--
-- TOC entry 2674 (class 2606 OID 19447)
-- Name: rmt_fee_product_type pk_rmt_fee_product_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_product_type
    ADD CONSTRAINT pk_rmt_fee_product_type PRIMARY KEY (fee_product_type_id, start_date);


--
-- TOC entry 2678 (class 2606 OID 19449)
-- Name: rmt_fee_promotion_type pk_rmt_fee_promo_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_promotion_type
    ADD CONSTRAINT pk_rmt_fee_promo_type PRIMARY KEY (fee_promotion_type_id, start_date);


--
-- TOC entry 2682 (class 2606 OID 19451)
-- Name: rmt_fee_service_type pk_rmt_fee_serv_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_service_type
    ADD CONSTRAINT pk_rmt_fee_serv_type PRIMARY KEY (fee_service_type_id, start_date);


--
-- TOC entry 2686 (class 2606 OID 19453)
-- Name: rmt_plan_charge_type pk_rmt_plan_charge_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_charge_type
    ADD CONSTRAINT pk_rmt_plan_charge_type PRIMARY KEY (plan_charge_type_id, start_date);


--
-- TOC entry 2690 (class 2606 OID 19455)
-- Name: rmt_plan_discount_type pk_rmt_plan_discount_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_discount_type
    ADD CONSTRAINT pk_rmt_plan_discount_type PRIMARY KEY (plan_discount_type_id, start_date);


--
-- TOC entry 2692 (class 2606 OID 19457)
-- Name: rmt_plan_prerreq_type pk_rmt_plan_prerreq_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_prerreq_type
    ADD CONSTRAINT pk_rmt_plan_prerreq_type PRIMARY KEY (plan_prerreq_type_id, start_date);


--
-- TOC entry 2696 (class 2606 OID 19459)
-- Name: rmt_plan_promotion_type pk_rmt_plan_promotion_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_promotion_type
    ADD CONSTRAINT pk_rmt_plan_promotion_type PRIMARY KEY (plan_promotion_type_id, start_date);


--
-- TOC entry 2700 (class 2606 OID 19461)
-- Name: rmt_promotion_equipment_type pk_rmt_prom_equip_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_equipment_type
    ADD CONSTRAINT pk_rmt_prom_equip_type PRIMARY KEY (prom_equip_type_id, start_date);


--
-- TOC entry 2704 (class 2606 OID 19463)
-- Name: rmt_promotion_product_type pk_rmt_prom_product_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_product_type
    ADD CONSTRAINT pk_rmt_prom_product_type PRIMARY KEY (prom_prod_type_id, start_date);


--
-- TOC entry 2708 (class 2606 OID 19465)
-- Name: rmt_promotion_service_type pk_rmt_prom_service_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_service_type
    ADD CONSTRAINT pk_rmt_prom_service_type PRIMARY KEY (prom_serv_type_id, start_date);


--
-- TOC entry 2714 (class 2606 OID 19467)
-- Name: rmt_tariff_equipment_type pk_rmt_tariff_equipment_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_equipment_type
    ADD CONSTRAINT pk_rmt_tariff_equipment_type PRIMARY KEY (tariff_equipment_type_id, start_date);


--
-- TOC entry 2718 (class 2606 OID 19469)
-- Name: rmt_tariff_product_type pk_rmt_tariff_product_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_product_type
    ADD CONSTRAINT pk_rmt_tariff_product_type PRIMARY KEY (tariff_product_type_id, start_date);


--
-- TOC entry 2722 (class 2606 OID 19471)
-- Name: rmt_tariff_promotion_type pk_rmt_tariff_prom_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_promotion_type
    ADD CONSTRAINT pk_rmt_tariff_prom_type PRIMARY KEY (tariff_promotion_type_id, start_date);


--
-- TOC entry 2726 (class 2606 OID 19473)
-- Name: rmt_tariff_service_type pk_rmt_tariff_service_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_service_type
    ADD CONSTRAINT pk_rmt_tariff_service_type PRIMARY KEY (tariff_service_type_id, start_date);


--
-- TOC entry 2712 (class 2606 OID 19475)
-- Name: rmt_service_equipment_type pk_service_equipment_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_service_equipment_type
    ADD CONSTRAINT pk_service_equipment_type PRIMARY KEY (service_equipment_type_id, start_date);


--
-- TOC entry 2730 (class 2606 OID 19477)
-- Name: test_menu pk_test_menu; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY test_menu
    ADD CONSTRAINT pk_test_menu PRIMARY KEY (menu_id);


--
-- TOC entry 2734 (class 2606 OID 19479)
-- Name: test_user pk_test_user; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY test_user
    ADD CONSTRAINT pk_test_user PRIMARY KEY (user_id, profile_id);


--
-- TOC entry 2738 (class 2606 OID 19481)
-- Name: tid_consumption_code_id pk_tid_consumption_code; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_consumption_code_id
    ADD CONSTRAINT pk_tid_consumption_code PRIMARY KEY (consumption_code_id);


--
-- TOC entry 2740 (class 2606 OID 19483)
-- Name: tid_equipment_type_id pk_tid_equipment_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_equipment_type_id
    ADD CONSTRAINT pk_tid_equipment_type PRIMARY KEY (equipment_type_id);


--
-- TOC entry 2742 (class 2606 OID 19485)
-- Name: tid_fee_code_id pk_tid_fee_code; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_fee_code_id
    ADD CONSTRAINT pk_tid_fee_code PRIMARY KEY (fee_code_id);


--
-- TOC entry 2744 (class 2606 OID 19487)
-- Name: tid_fee_equipment_type_id pk_tid_fee_equipment_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_fee_equipment_type_id
    ADD CONSTRAINT pk_tid_fee_equipment_type PRIMARY KEY (fee_equipment_type_id);


--
-- TOC entry 2746 (class 2606 OID 19489)
-- Name: tid_fee_product_type_id pk_tid_fee_product_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_fee_product_type_id
    ADD CONSTRAINT pk_tid_fee_product_type PRIMARY KEY (fee_product_type_id);


--
-- TOC entry 2748 (class 2606 OID 19491)
-- Name: tid_fee_promotion_type_id pk_tid_fee_promotion_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_fee_promotion_type_id
    ADD CONSTRAINT pk_tid_fee_promotion_type PRIMARY KEY (fee_promotion_type_id);


--
-- TOC entry 2750 (class 2606 OID 19493)
-- Name: tid_fee_service_type_id pk_tid_fee_service_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_fee_service_type_id
    ADD CONSTRAINT pk_tid_fee_service_type PRIMARY KEY (fee_service_type_id);


--
-- TOC entry 2752 (class 2606 OID 19495)
-- Name: tid_plan_charge_type_id pk_tid_plan_charge_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_plan_charge_type_id
    ADD CONSTRAINT pk_tid_plan_charge_type PRIMARY KEY (plan_charge_type_id);


--
-- TOC entry 2754 (class 2606 OID 19497)
-- Name: tid_plan_discount_type_id pk_tid_plan_discount_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_plan_discount_type_id
    ADD CONSTRAINT pk_tid_plan_discount_type PRIMARY KEY (plan_discount_type_id);


--
-- TOC entry 2756 (class 2606 OID 19499)
-- Name: tid_plan_prerreq_type_id pk_tid_plan_prerreq_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_plan_prerreq_type_id
    ADD CONSTRAINT pk_tid_plan_prerreq_type PRIMARY KEY (plan_prerreq_type_id);


--
-- TOC entry 2758 (class 2606 OID 19501)
-- Name: tid_plan_promotion_type_id pk_tid_plan_promotion_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_plan_promotion_type_id
    ADD CONSTRAINT pk_tid_plan_promotion_type PRIMARY KEY (plan_promotion_type_id);


--
-- TOC entry 2760 (class 2606 OID 19503)
-- Name: tid_plan_type_id pk_tid_plan_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_plan_type_id
    ADD CONSTRAINT pk_tid_plan_type PRIMARY KEY (plan_type_id);


--
-- TOC entry 2762 (class 2606 OID 19505)
-- Name: tid_product_service_type_id pk_tid_product_service_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_product_service_type_id
    ADD CONSTRAINT pk_tid_product_service_type PRIMARY KEY (product_service_type_id);


--
-- TOC entry 2764 (class 2606 OID 19507)
-- Name: tid_product_type_id pk_tid_product_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_product_type_id
    ADD CONSTRAINT pk_tid_product_type PRIMARY KEY (product_type_id);


--
-- TOC entry 2766 (class 2606 OID 19509)
-- Name: tid_profile_id pk_tid_profile; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_profile_id
    ADD CONSTRAINT pk_tid_profile PRIMARY KEY (profile_id);


--
-- TOC entry 2768 (class 2606 OID 19511)
-- Name: tid_promotion_equipment_type_id pk_tid_prom_equip_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_promotion_equipment_type_id
    ADD CONSTRAINT pk_tid_prom_equip_type PRIMARY KEY (prom_equip_type_id);


--
-- TOC entry 2770 (class 2606 OID 19513)
-- Name: tid_promotion_product_type_id pk_tid_prom_prod_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_promotion_product_type_id
    ADD CONSTRAINT pk_tid_prom_prod_type PRIMARY KEY (prom_prod_type_id);


--
-- TOC entry 2772 (class 2606 OID 19515)
-- Name: tid_promotion_promotion_type_id pk_tid_prom_prom_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_promotion_promotion_type_id
    ADD CONSTRAINT pk_tid_prom_prom_type PRIMARY KEY (prom_prom_type_id);


--
-- TOC entry 2774 (class 2606 OID 19517)
-- Name: tid_promotion_service_type_id pk_tid_prom_serv_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_promotion_service_type_id
    ADD CONSTRAINT pk_tid_prom_serv_type PRIMARY KEY (prom_serv_type_id);


--
-- TOC entry 2776 (class 2606 OID 19519)
-- Name: tid_promotion_type_id pk_tid_promotion_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_promotion_type_id
    ADD CONSTRAINT pk_tid_promotion_type PRIMARY KEY (promotion_type_id);


--
-- TOC entry 2778 (class 2606 OID 19521)
-- Name: tid_service_equipment_type_id pk_tid_service_equipment_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_service_equipment_type_id
    ADD CONSTRAINT pk_tid_service_equipment_type PRIMARY KEY (service_equipment_type_id);


--
-- TOC entry 2780 (class 2606 OID 19523)
-- Name: tid_service_type_id pk_tid_service_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_service_type_id
    ADD CONSTRAINT pk_tid_service_type PRIMARY KEY (service_type_id);


--
-- TOC entry 2782 (class 2606 OID 19525)
-- Name: tid_tariff_equipment_type_id pk_tid_tariff_equipment_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_tariff_equipment_type_id
    ADD CONSTRAINT pk_tid_tariff_equipment_type PRIMARY KEY (tariff_equipment_type_id);


--
-- TOC entry 2784 (class 2606 OID 19527)
-- Name: tid_tariff_product_type_id pk_tid_tariff_product_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_tariff_product_type_id
    ADD CONSTRAINT pk_tid_tariff_product_type PRIMARY KEY (tariff_product_type_id);


--
-- TOC entry 2786 (class 2606 OID 19529)
-- Name: tid_tariff_promotion_type_id pk_tid_tariff_promotion_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_tariff_promotion_type_id
    ADD CONSTRAINT pk_tid_tariff_promotion_type PRIMARY KEY (tariff_promotion_type_id);


--
-- TOC entry 2788 (class 2606 OID 19531)
-- Name: tid_tariff_service_type_id pk_tid_tariff_service_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_tariff_service_type_id
    ADD CONSTRAINT pk_tid_tariff_service_type PRIMARY KEY (tariff_service_type_id);


--
-- TOC entry 2790 (class 2606 OID 19533)
-- Name: tid_tariff_type_id pk_tid_tariff_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_tariff_type_id
    ADD CONSTRAINT pk_tid_tariff_type PRIMARY KEY (tariff_type_id);


--
-- TOC entry 2792 (class 2606 OID 19535)
-- Name: tid_user_id pk_tid_user; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY tid_user_id
    ADD CONSTRAINT pk_tid_user PRIMARY KEY (user_id);


--
-- TOC entry 2732 (class 2606 OID 19537)
-- Name: test_menu u_test_menu; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY test_menu
    ADD CONSTRAINT u_test_menu UNIQUE (menu_code, profile_code);


--
-- TOC entry 2736 (class 2606 OID 19539)
-- Name: test_user u_test_user; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY test_user
    ADD CONSTRAINT u_test_user UNIQUE (user_code, profile_code);


--
-- TOC entry 2600 (class 2606 OID 19541)
-- Name: it_user uk_it_user_end_date; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY it_user
    ADD CONSTRAINT uk_it_user_end_date UNIQUE (user_code, end_date);


--
-- TOC entry 2602 (class 2606 OID 19543)
-- Name: it_user uk_it_user_start_date; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY it_user
    ADD CONSTRAINT uk_it_user_start_date UNIQUE (user_code, start_date);


--
-- TOC entry 2606 (class 2606 OID 19545)
-- Name: mt_application_level uk_mt_application_level; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_application_level
    ADD CONSTRAINT uk_mt_application_level UNIQUE (application_level_code);


--
-- TOC entry 2610 (class 2606 OID 19547)
-- Name: mt_application_unit uk_mt_application_unit; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_application_unit
    ADD CONSTRAINT uk_mt_application_unit UNIQUE (application_unit_code);


--
-- TOC entry 2614 (class 2606 OID 19549)
-- Name: mt_business_scope uk_mt_business_scope; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_business_scope
    ADD CONSTRAINT uk_mt_business_scope UNIQUE (business_scope_code);


--
-- TOC entry 2620 (class 2606 OID 19551)
-- Name: mt_consumption_type uk_mt_consumption_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_consumption_type
    ADD CONSTRAINT uk_mt_consumption_type UNIQUE (consumption_type_code);


--
-- TOC entry 2624 (class 2606 OID 19553)
-- Name: mt_discount_concept uk_mt_discount_concept; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_discount_concept
    ADD CONSTRAINT uk_mt_discount_concept UNIQUE (discount_concept_code);


--
-- TOC entry 2628 (class 2606 OID 19555)
-- Name: mt_discount_type uk_mt_discount_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_discount_type
    ADD CONSTRAINT uk_mt_discount_type UNIQUE (discount_type_code);


--
-- TOC entry 2632 (class 2606 OID 19557)
-- Name: mt_entity_type uk_mt_entity_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_entity_type
    ADD CONSTRAINT uk_mt_entity_type UNIQUE (entity_type_code);


--
-- TOC entry 2642 (class 2606 OID 19559)
-- Name: mt_product_type uk_mt_product_type_end_date; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_product_type
    ADD CONSTRAINT uk_mt_product_type_end_date UNIQUE (product_type_code, end_date);


--
-- TOC entry 2644 (class 2606 OID 19561)
-- Name: mt_product_type uk_mt_product_type_start_date; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_product_type
    ADD CONSTRAINT uk_mt_product_type_start_date UNIQUE (product_type_code, start_date);


--
-- TOC entry 2652 (class 2606 OID 19563)
-- Name: mt_recurrence uk_mt_recurrence; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_recurrence
    ADD CONSTRAINT uk_mt_recurrence UNIQUE (recurrence_code);


--
-- TOC entry 2656 (class 2606 OID 19565)
-- Name: mt_service_type uk_mt_service_type_end_date; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_service_type
    ADD CONSTRAINT uk_mt_service_type_end_date UNIQUE (service_type_code, end_date);


--
-- TOC entry 2658 (class 2606 OID 19567)
-- Name: mt_service_type uk_mt_service_type_start_date; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_service_type
    ADD CONSTRAINT uk_mt_service_type_start_date UNIQUE (service_type_code, start_date);


--
-- TOC entry 2662 (class 2606 OID 19569)
-- Name: mt_status uk_mt_status; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_status
    ADD CONSTRAINT uk_mt_status UNIQUE (status_id, entity_type_id, status_code);


--
-- TOC entry 2668 (class 2606 OID 19571)
-- Name: mt_technology_scope uk_mt_technology_scope; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_technology_scope
    ADD CONSTRAINT uk_mt_technology_scope UNIQUE (technology_scope_code);


--
-- TOC entry 2672 (class 2606 OID 19573)
-- Name: rmt_fee_equip_type uk_rmt_fee_equip_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_equip_type
    ADD CONSTRAINT uk_rmt_fee_equip_type UNIQUE (fee_code_id, equipment_type_id, start_date);


--
-- TOC entry 2676 (class 2606 OID 19575)
-- Name: rmt_fee_product_type uk_rmt_fee_product_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_product_type
    ADD CONSTRAINT uk_rmt_fee_product_type UNIQUE (fee_code_id, product_type_id, start_date);


--
-- TOC entry 2680 (class 2606 OID 19577)
-- Name: rmt_fee_promotion_type uk_rmt_fee_promo_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_promotion_type
    ADD CONSTRAINT uk_rmt_fee_promo_type UNIQUE (fee_code_id, promotion_type_id, start_date);


--
-- TOC entry 2684 (class 2606 OID 19579)
-- Name: rmt_fee_service_type uk_rmt_fee_serv_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_service_type
    ADD CONSTRAINT uk_rmt_fee_serv_type UNIQUE (fee_code_id, service_type_id, start_date);


--
-- TOC entry 2688 (class 2606 OID 19581)
-- Name: rmt_plan_charge_type uk_rmt_plan_charge_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_charge_type
    ADD CONSTRAINT uk_rmt_plan_charge_type UNIQUE (plan_type_id, charge_type_id, start_date);


--
-- TOC entry 2694 (class 2606 OID 19583)
-- Name: rmt_plan_prerreq_type uk_rmt_plan_prerreq_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_prerreq_type
    ADD CONSTRAINT uk_rmt_plan_prerreq_type UNIQUE (plan_type_id, prerrequisite_type_id, start_date);


--
-- TOC entry 2698 (class 2606 OID 19585)
-- Name: rmt_plan_promotion_type uk_rmt_plan_promotion_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_promotion_type
    ADD CONSTRAINT uk_rmt_plan_promotion_type UNIQUE (promotion_type_id, plan_type_id, start_date);


--
-- TOC entry 2702 (class 2606 OID 19587)
-- Name: rmt_promotion_equipment_type uk_rmt_prom_equip_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_equipment_type
    ADD CONSTRAINT uk_rmt_prom_equip_type UNIQUE (promotion_type_id, equipment_type_id, start_date);


--
-- TOC entry 2706 (class 2606 OID 19589)
-- Name: rmt_promotion_product_type uk_rmt_prom_product_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_product_type
    ADD CONSTRAINT uk_rmt_prom_product_type UNIQUE (promotion_type_id, product_type_id, start_date);


--
-- TOC entry 2710 (class 2606 OID 19591)
-- Name: rmt_promotion_service_type uk_rmt_prom_service_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_service_type
    ADD CONSTRAINT uk_rmt_prom_service_type UNIQUE (promotion_type_id, service_type_id, start_date);


--
-- TOC entry 2716 (class 2606 OID 19593)
-- Name: rmt_tariff_equipment_type uk_rmt_tariff_equipment_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_equipment_type
    ADD CONSTRAINT uk_rmt_tariff_equipment_type UNIQUE (tariff_type_id, equipment_type_id, start_date);


--
-- TOC entry 2720 (class 2606 OID 19595)
-- Name: rmt_tariff_product_type uk_rmt_tariff_product_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_product_type
    ADD CONSTRAINT uk_rmt_tariff_product_type UNIQUE (tariff_type_id, product_type_id, start_date);


--
-- TOC entry 2724 (class 2606 OID 19597)
-- Name: rmt_tariff_promotion_type uk_rmt_tariff_prom_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_promotion_type
    ADD CONSTRAINT uk_rmt_tariff_prom_type UNIQUE (tariff_type_id, promotion_type_id, start_date);


--
-- TOC entry 2728 (class 2606 OID 19599)
-- Name: rmt_tariff_service_type uk_rmt_tariff_service_type; Type: CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_service_type
    ADD CONSTRAINT uk_rmt_tariff_service_type UNIQUE (tariff_type_id, service_type_id, start_date);


--
-- TOC entry 2928 (class 2620 OID 19600)
-- Name: it_user trg_it_user_add_user_validation; Type: TRIGGER; Schema: billing; Owner: billing_admin
--

CREATE TRIGGER trg_it_user_add_user_validation BEFORE INSERT OR UPDATE ON it_user FOR EACH ROW EXECUTE PROCEDURE tf_add_user_validation();


--
-- TOC entry 2929 (class 2620 OID 19601)
-- Name: it_user trg_it_user_format_default_dates; Type: TRIGGER; Schema: billing; Owner: billing_admin
--

CREATE TRIGGER trg_it_user_format_default_dates BEFORE INSERT OR UPDATE ON it_user FOR EACH ROW EXECUTE PROCEDURE tf_format_default_dates();


--
-- TOC entry 2930 (class 2620 OID 19602)
-- Name: mt_product_type trg_mt_product_type_add_product_type_validation; Type: TRIGGER; Schema: billing; Owner: billing_admin
--

CREATE TRIGGER trg_mt_product_type_add_product_type_validation BEFORE INSERT OR UPDATE ON mt_product_type FOR EACH ROW EXECUTE PROCEDURE tf_add_product_type_validation();


--
-- TOC entry 2931 (class 2620 OID 19603)
-- Name: mt_product_type trg_mt_product_type_add_status_entity_validation; Type: TRIGGER; Schema: billing; Owner: billing_admin
--

CREATE TRIGGER trg_mt_product_type_add_status_entity_validation BEFORE INSERT OR UPDATE ON mt_product_type FOR EACH ROW EXECUTE PROCEDURE tf_status_entity_type_validation();


--
-- TOC entry 2932 (class 2620 OID 19604)
-- Name: mt_product_type trg_mt_product_type_format_default_dates; Type: TRIGGER; Schema: billing; Owner: billing_admin
--

CREATE TRIGGER trg_mt_product_type_format_default_dates BEFORE INSERT OR UPDATE ON mt_product_type FOR EACH ROW EXECUTE PROCEDURE tf_format_default_dates();


--
-- TOC entry 2933 (class 2620 OID 19605)
-- Name: mt_service_type trg_mt_service_type_add_service_type_validation; Type: TRIGGER; Schema: billing; Owner: billing_admin
--

CREATE TRIGGER trg_mt_service_type_add_service_type_validation BEFORE INSERT OR UPDATE ON mt_service_type FOR EACH ROW EXECUTE PROCEDURE tf_add_service_type_validation();


--
-- TOC entry 2934 (class 2620 OID 19606)
-- Name: mt_service_type trg_mt_service_type_add_status_entity_validation; Type: TRIGGER; Schema: billing; Owner: billing_admin
--

CREATE TRIGGER trg_mt_service_type_add_status_entity_validation BEFORE INSERT OR UPDATE ON mt_service_type FOR EACH ROW EXECUTE PROCEDURE tf_status_entity_type_validation();


--
-- TOC entry 2935 (class 2620 OID 19607)
-- Name: mt_service_type trg_mt_service_type_format_default_dates; Type: TRIGGER; Schema: billing; Owner: billing_admin
--

CREATE TRIGGER trg_mt_service_type_format_default_dates BEFORE INSERT OR UPDATE ON mt_service_type FOR EACH ROW EXECUTE PROCEDURE tf_format_default_dates();


--
-- TOC entry 2793 (class 2606 OID 19608)
-- Name: it_user fk_it_user; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY it_user
    ADD CONSTRAINT fk_it_user FOREIGN KEY (user_id) REFERENCES tid_user_id(user_id) ON DELETE CASCADE;


--
-- TOC entry 2794 (class 2606 OID 19613)
-- Name: it_user fk_it_user_profile; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY it_user
    ADD CONSTRAINT fk_it_user_profile FOREIGN KEY (profile_id) REFERENCES tid_profile_id(profile_id);


--
-- TOC entry 2795 (class 2606 OID 19618)
-- Name: it_user fk_it_user_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY it_user
    ADD CONSTRAINT fk_it_user_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2927 (class 2606 OID 19623)
-- Name: test_menu fk_menu_item; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY test_menu
    ADD CONSTRAINT fk_menu_item FOREIGN KEY (submenu_id) REFERENCES test_menu(menu_id);


--
-- TOC entry 2796 (class 2606 OID 19628)
-- Name: mt_consumption_code fk_mt_cons_code_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_consumption_code
    ADD CONSTRAINT fk_mt_cons_code_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2797 (class 2606 OID 19633)
-- Name: mt_consumption_code fk_mt_cons_code_consumpt; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_consumption_code
    ADD CONSTRAINT fk_mt_cons_code_consumpt FOREIGN KEY (consumption_type_id) REFERENCES mt_consumption_type(consumption_type_id);


--
-- TOC entry 2798 (class 2606 OID 19638)
-- Name: mt_consumption_code fk_mt_cons_code_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_consumption_code
    ADD CONSTRAINT fk_mt_cons_code_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2799 (class 2606 OID 19643)
-- Name: mt_consumption_code fk_mt_cons_code_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_consumption_code
    ADD CONSTRAINT fk_mt_cons_code_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2800 (class 2606 OID 19648)
-- Name: mt_consumption_code fk_mt_consumption_code; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_consumption_code
    ADD CONSTRAINT fk_mt_consumption_code FOREIGN KEY (consumption_code_id) REFERENCES tid_consumption_code_id(consumption_code_id) ON DELETE CASCADE;


--
-- TOC entry 2801 (class 2606 OID 19653)
-- Name: mt_equipment_type fk_mt_equip_type_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_equipment_type
    ADD CONSTRAINT fk_mt_equip_type_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2802 (class 2606 OID 19658)
-- Name: mt_equipment_type fk_mt_equip_type_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_equipment_type
    ADD CONSTRAINT fk_mt_equip_type_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2803 (class 2606 OID 19663)
-- Name: mt_equipment_type fk_mt_equip_type_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_equipment_type
    ADD CONSTRAINT fk_mt_equip_type_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2804 (class 2606 OID 19668)
-- Name: mt_equipment_type fk_mt_equipment_type; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_equipment_type
    ADD CONSTRAINT fk_mt_equipment_type FOREIGN KEY (equipment_type_id) REFERENCES tid_equipment_type_id(equipment_type_id) ON DELETE CASCADE;


--
-- TOC entry 2805 (class 2606 OID 19673)
-- Name: mt_fee_code fk_mt_fee_code; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_fee_code
    ADD CONSTRAINT fk_mt_fee_code FOREIGN KEY (fee_code_id) REFERENCES tid_fee_code_id(fee_code_id) ON DELETE CASCADE;


--
-- TOC entry 2806 (class 2606 OID 19678)
-- Name: mt_fee_code fk_mt_fee_code_appl_level; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_fee_code
    ADD CONSTRAINT fk_mt_fee_code_appl_level FOREIGN KEY (application_level_id) REFERENCES mt_application_level(application_level_id);


--
-- TOC entry 2807 (class 2606 OID 19683)
-- Name: mt_fee_code fk_mt_fee_code_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_fee_code
    ADD CONSTRAINT fk_mt_fee_code_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2808 (class 2606 OID 19688)
-- Name: mt_fee_code fk_mt_fee_code_recurrence; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_fee_code
    ADD CONSTRAINT fk_mt_fee_code_recurrence FOREIGN KEY (recurrence_id) REFERENCES mt_recurrence(recurrence_id);


--
-- TOC entry 2809 (class 2606 OID 19693)
-- Name: mt_fee_code fk_mt_fee_code_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_fee_code
    ADD CONSTRAINT fk_mt_fee_code_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2810 (class 2606 OID 19698)
-- Name: mt_fee_code fk_mt_fee_code_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_fee_code
    ADD CONSTRAINT fk_mt_fee_code_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2811 (class 2606 OID 19703)
-- Name: mt_plan_type fk_mt_plan_type; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_plan_type
    ADD CONSTRAINT fk_mt_plan_type FOREIGN KEY (plan_type_id) REFERENCES tid_plan_type_id(plan_type_id) ON DELETE CASCADE;


--
-- TOC entry 2812 (class 2606 OID 19708)
-- Name: mt_plan_type fk_mt_plan_type_appl_level; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_plan_type
    ADD CONSTRAINT fk_mt_plan_type_appl_level FOREIGN KEY (application_level_id) REFERENCES mt_application_level(application_level_id);


--
-- TOC entry 2813 (class 2606 OID 19713)
-- Name: mt_plan_type fk_mt_plan_type_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_plan_type
    ADD CONSTRAINT fk_mt_plan_type_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2814 (class 2606 OID 19718)
-- Name: mt_plan_type fk_mt_plan_type_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_plan_type
    ADD CONSTRAINT fk_mt_plan_type_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2815 (class 2606 OID 19723)
-- Name: mt_plan_type fk_mt_plan_type_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_plan_type
    ADD CONSTRAINT fk_mt_plan_type_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2816 (class 2606 OID 19728)
-- Name: mt_product_type fk_mt_product_type; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_product_type
    ADD CONSTRAINT fk_mt_product_type FOREIGN KEY (product_type_id) REFERENCES tid_product_type_id(product_type_id) ON DELETE CASCADE;


--
-- TOC entry 2817 (class 2606 OID 19733)
-- Name: mt_product_type fk_mt_product_type_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_product_type
    ADD CONSTRAINT fk_mt_product_type_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2818 (class 2606 OID 19738)
-- Name: mt_product_type fk_mt_product_type_entity_type_id; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_product_type
    ADD CONSTRAINT fk_mt_product_type_entity_type_id FOREIGN KEY (entity_type_id) REFERENCES mt_entity_type(entity_type_id);


--
-- TOC entry 2819 (class 2606 OID 19743)
-- Name: mt_product_type fk_mt_product_type_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_product_type
    ADD CONSTRAINT fk_mt_product_type_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2820 (class 2606 OID 19748)
-- Name: mt_product_type fk_mt_product_type_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_product_type
    ADD CONSTRAINT fk_mt_product_type_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2821 (class 2606 OID 19753)
-- Name: mt_profile fk_mt_profile; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_profile
    ADD CONSTRAINT fk_mt_profile FOREIGN KEY (profile_id) REFERENCES tid_profile_id(profile_id) ON DELETE CASCADE;


--
-- TOC entry 2822 (class 2606 OID 19758)
-- Name: mt_promotion_type fk_mt_promotion_type; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_promotion_type
    ADD CONSTRAINT fk_mt_promotion_type FOREIGN KEY (promotion_type_id) REFERENCES tid_promotion_type_id(promotion_type_id) ON DELETE CASCADE;


--
-- TOC entry 2823 (class 2606 OID 19763)
-- Name: mt_promotion_type fk_mt_promotion_type_appl_level; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_promotion_type
    ADD CONSTRAINT fk_mt_promotion_type_appl_level FOREIGN KEY (application_level_id) REFERENCES mt_application_level(application_level_id);


--
-- TOC entry 2824 (class 2606 OID 19768)
-- Name: mt_promotion_type fk_mt_promotion_type_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_promotion_type
    ADD CONSTRAINT fk_mt_promotion_type_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2825 (class 2606 OID 19773)
-- Name: mt_promotion_type fk_mt_promotion_type_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_promotion_type
    ADD CONSTRAINT fk_mt_promotion_type_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2826 (class 2606 OID 19778)
-- Name: mt_promotion_type fk_mt_promotion_type_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_promotion_type
    ADD CONSTRAINT fk_mt_promotion_type_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2827 (class 2606 OID 19783)
-- Name: mt_service_type fk_mt_service_type; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_service_type
    ADD CONSTRAINT fk_mt_service_type FOREIGN KEY (service_type_id) REFERENCES tid_service_type_id(service_type_id) ON DELETE CASCADE;


--
-- TOC entry 2828 (class 2606 OID 19788)
-- Name: mt_service_type fk_mt_service_type_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_service_type
    ADD CONSTRAINT fk_mt_service_type_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2829 (class 2606 OID 19793)
-- Name: mt_service_type fk_mt_service_type_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_service_type
    ADD CONSTRAINT fk_mt_service_type_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2830 (class 2606 OID 19798)
-- Name: mt_service_type fk_mt_service_type_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_service_type
    ADD CONSTRAINT fk_mt_service_type_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2831 (class 2606 OID 19803)
-- Name: mt_status fk_mt_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_status
    ADD CONSTRAINT fk_mt_status FOREIGN KEY (entity_type_id) REFERENCES mt_entity_type(entity_type_id) ON DELETE CASCADE;


--
-- TOC entry 2832 (class 2606 OID 19808)
-- Name: mt_tariff_type fk_mt_tariff_type; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_tariff_type
    ADD CONSTRAINT fk_mt_tariff_type FOREIGN KEY (tariff_type_id) REFERENCES tid_tariff_type_id(tariff_type_id) ON DELETE CASCADE;


--
-- TOC entry 2833 (class 2606 OID 19813)
-- Name: mt_tariff_type fk_mt_tariff_type_appl_level; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_tariff_type
    ADD CONSTRAINT fk_mt_tariff_type_appl_level FOREIGN KEY (application_level_id) REFERENCES mt_application_level(application_level_id);


--
-- TOC entry 2834 (class 2606 OID 19818)
-- Name: mt_tariff_type fk_mt_tariff_type_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_tariff_type
    ADD CONSTRAINT fk_mt_tariff_type_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2835 (class 2606 OID 19823)
-- Name: mt_tariff_type fk_mt_tariff_type_recurrence; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_tariff_type
    ADD CONSTRAINT fk_mt_tariff_type_recurrence FOREIGN KEY (recurrence_id) REFERENCES mt_recurrence(recurrence_id);


--
-- TOC entry 2836 (class 2606 OID 19828)
-- Name: mt_tariff_type fk_mt_tariff_type_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_tariff_type
    ADD CONSTRAINT fk_mt_tariff_type_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2837 (class 2606 OID 19833)
-- Name: mt_tariff_type fk_mt_tariff_type_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY mt_tariff_type
    ADD CONSTRAINT fk_mt_tariff_type_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2838 (class 2606 OID 19838)
-- Name: rmt_fee_equip_type fk_rmt_fee_equ_t_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_equip_type
    ADD CONSTRAINT fk_rmt_fee_equ_t_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2839 (class 2606 OID 19843)
-- Name: rmt_fee_equip_type fk_rmt_fee_equ_t_eq_id; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_equip_type
    ADD CONSTRAINT fk_rmt_fee_equ_t_eq_id FOREIGN KEY (equipment_type_id) REFERENCES tid_equipment_type_id(equipment_type_id);


--
-- TOC entry 2840 (class 2606 OID 19848)
-- Name: rmt_fee_equip_type fk_rmt_fee_equ_t_fee_id; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_equip_type
    ADD CONSTRAINT fk_rmt_fee_equ_t_fee_id FOREIGN KEY (fee_code_id) REFERENCES tid_fee_code_id(fee_code_id);


--
-- TOC entry 2850 (class 2606 OID 19853)
-- Name: rmt_fee_promotion_type fk_rmt_fee_equ_t_fee_id; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_promotion_type
    ADD CONSTRAINT fk_rmt_fee_equ_t_fee_id FOREIGN KEY (fee_code_id) REFERENCES tid_fee_code_id(fee_code_id);


--
-- TOC entry 2841 (class 2606 OID 19858)
-- Name: rmt_fee_equip_type fk_rmt_fee_equ_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_equip_type
    ADD CONSTRAINT fk_rmt_fee_equ_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2842 (class 2606 OID 19863)
-- Name: rmt_fee_equip_type fk_rmt_fee_equ_t_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_equip_type
    ADD CONSTRAINT fk_rmt_fee_equ_t_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2843 (class 2606 OID 19868)
-- Name: rmt_fee_equip_type fk_rmt_fee_equip_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_equip_type
    ADD CONSTRAINT fk_rmt_fee_equip_t FOREIGN KEY (fee_equipment_type_id) REFERENCES tid_fee_equipment_type_id(fee_equipment_type_id) ON DELETE CASCADE;


--
-- TOC entry 2844 (class 2606 OID 19873)
-- Name: rmt_fee_product_type fk_rmt_fee_prod_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_product_type
    ADD CONSTRAINT fk_rmt_fee_prod_t FOREIGN KEY (fee_product_type_id) REFERENCES tid_fee_product_type_id(fee_product_type_id) ON DELETE CASCADE;


--
-- TOC entry 2845 (class 2606 OID 19878)
-- Name: rmt_fee_product_type fk_rmt_fee_prod_t_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_product_type
    ADD CONSTRAINT fk_rmt_fee_prod_t_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2846 (class 2606 OID 19883)
-- Name: rmt_fee_product_type fk_rmt_fee_prod_t_fee_id; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_product_type
    ADD CONSTRAINT fk_rmt_fee_prod_t_fee_id FOREIGN KEY (fee_code_id) REFERENCES tid_fee_code_id(fee_code_id);


--
-- TOC entry 2847 (class 2606 OID 19888)
-- Name: rmt_fee_product_type fk_rmt_fee_prod_t_prod_id; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_product_type
    ADD CONSTRAINT fk_rmt_fee_prod_t_prod_id FOREIGN KEY (product_type_id) REFERENCES tid_product_type_id(product_type_id);


--
-- TOC entry 2848 (class 2606 OID 19893)
-- Name: rmt_fee_product_type fk_rmt_fee_prod_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_product_type
    ADD CONSTRAINT fk_rmt_fee_prod_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2849 (class 2606 OID 19898)
-- Name: rmt_fee_product_type fk_rmt_fee_prod_t_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_product_type
    ADD CONSTRAINT fk_rmt_fee_prod_t_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2851 (class 2606 OID 19903)
-- Name: rmt_fee_promotion_type fk_rmt_fee_prom_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_promotion_type
    ADD CONSTRAINT fk_rmt_fee_prom_t FOREIGN KEY (fee_promotion_type_id) REFERENCES tid_fee_promotion_type_id(fee_promotion_type_id) ON DELETE CASCADE;


--
-- TOC entry 2852 (class 2606 OID 19908)
-- Name: rmt_fee_promotion_type fk_rmt_fee_prom_t_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_promotion_type
    ADD CONSTRAINT fk_rmt_fee_prom_t_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2853 (class 2606 OID 19913)
-- Name: rmt_fee_promotion_type fk_rmt_fee_prom_t_prod_id; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_promotion_type
    ADD CONSTRAINT fk_rmt_fee_prom_t_prod_id FOREIGN KEY (promotion_type_id) REFERENCES tid_promotion_type_id(promotion_type_id);


--
-- TOC entry 2854 (class 2606 OID 19918)
-- Name: rmt_fee_promotion_type fk_rmt_fee_prom_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_promotion_type
    ADD CONSTRAINT fk_rmt_fee_prom_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2855 (class 2606 OID 19923)
-- Name: rmt_fee_promotion_type fk_rmt_fee_prom_t_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_promotion_type
    ADD CONSTRAINT fk_rmt_fee_prom_t_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2856 (class 2606 OID 19928)
-- Name: rmt_fee_service_type fk_rmt_fee_serv_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_service_type
    ADD CONSTRAINT fk_rmt_fee_serv_t FOREIGN KEY (fee_service_type_id) REFERENCES tid_fee_service_type_id(fee_service_type_id) ON DELETE CASCADE;


--
-- TOC entry 2857 (class 2606 OID 19933)
-- Name: rmt_fee_service_type fk_rmt_fee_serv_t_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_service_type
    ADD CONSTRAINT fk_rmt_fee_serv_t_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2858 (class 2606 OID 19938)
-- Name: rmt_fee_service_type fk_rmt_fee_serv_t_fee_id; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_service_type
    ADD CONSTRAINT fk_rmt_fee_serv_t_fee_id FOREIGN KEY (fee_code_id) REFERENCES tid_fee_code_id(fee_code_id);


--
-- TOC entry 2859 (class 2606 OID 19943)
-- Name: rmt_fee_service_type fk_rmt_fee_serv_t_prod_id; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_service_type
    ADD CONSTRAINT fk_rmt_fee_serv_t_prod_id FOREIGN KEY (fee_code_id) REFERENCES tid_service_type_id(service_type_id);


--
-- TOC entry 2860 (class 2606 OID 19948)
-- Name: rmt_fee_service_type fk_rmt_fee_serv_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_service_type
    ADD CONSTRAINT fk_rmt_fee_serv_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2861 (class 2606 OID 19953)
-- Name: rmt_fee_service_type fk_rmt_fee_serv_t_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_fee_service_type
    ADD CONSTRAINT fk_rmt_fee_serv_t_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2862 (class 2606 OID 19958)
-- Name: rmt_plan_charge_type fk_rmt_plan_charge_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_charge_type
    ADD CONSTRAINT fk_rmt_plan_charge_t FOREIGN KEY (plan_charge_type_id) REFERENCES tid_plan_charge_type_id(plan_charge_type_id) ON DELETE CASCADE;


--
-- TOC entry 2863 (class 2606 OID 19963)
-- Name: rmt_plan_charge_type fk_rmt_plan_charge_t_plan; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_charge_type
    ADD CONSTRAINT fk_rmt_plan_charge_t_plan FOREIGN KEY (plan_type_id) REFERENCES tid_plan_type_id(plan_type_id);


--
-- TOC entry 2864 (class 2606 OID 19968)
-- Name: rmt_plan_charge_type fk_rmt_plan_charge_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_charge_type
    ADD CONSTRAINT fk_rmt_plan_charge_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2865 (class 2606 OID 19973)
-- Name: rmt_plan_discount_type fk_rmt_plan_discount_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_discount_type
    ADD CONSTRAINT fk_rmt_plan_discount_t FOREIGN KEY (plan_discount_type_id) REFERENCES tid_plan_discount_type_id(plan_discount_type_id) ON DELETE CASCADE;


--
-- TOC entry 2866 (class 2606 OID 19978)
-- Name: rmt_plan_discount_type fk_rmt_plan_discount_t_disc; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_discount_type
    ADD CONSTRAINT fk_rmt_plan_discount_t_disc FOREIGN KEY (discount_unit_id) REFERENCES mt_application_unit(application_unit_id);


--
-- TOC entry 2867 (class 2606 OID 19983)
-- Name: rmt_plan_discount_type fk_rmt_plan_discount_t_plan; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_discount_type
    ADD CONSTRAINT fk_rmt_plan_discount_t_plan FOREIGN KEY (plan_type_id) REFERENCES tid_plan_type_id(plan_type_id);


--
-- TOC entry 2868 (class 2606 OID 19988)
-- Name: rmt_plan_discount_type fk_rmt_plan_discount_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_discount_type
    ADD CONSTRAINT fk_rmt_plan_discount_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2869 (class 2606 OID 19993)
-- Name: rmt_plan_discount_type fk_rmt_plan_discount_t_thre; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_discount_type
    ADD CONSTRAINT fk_rmt_plan_discount_t_thre FOREIGN KEY (threshold_unit_id) REFERENCES mt_application_unit(application_unit_id);


--
-- TOC entry 2870 (class 2606 OID 19998)
-- Name: rmt_plan_prerreq_type fk_rmt_plan_prerreq_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_prerreq_type
    ADD CONSTRAINT fk_rmt_plan_prerreq_t FOREIGN KEY (plan_prerreq_type_id) REFERENCES tid_plan_prerreq_type_id(plan_prerreq_type_id) ON DELETE CASCADE;


--
-- TOC entry 2871 (class 2606 OID 20003)
-- Name: rmt_plan_prerreq_type fk_rmt_plan_prerreq_t_plan; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_prerreq_type
    ADD CONSTRAINT fk_rmt_plan_prerreq_t_plan FOREIGN KEY (plan_type_id) REFERENCES tid_plan_type_id(plan_type_id);


--
-- TOC entry 2872 (class 2606 OID 20008)
-- Name: rmt_plan_prerreq_type fk_rmt_plan_prerreq_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_prerreq_type
    ADD CONSTRAINT fk_rmt_plan_prerreq_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2873 (class 2606 OID 20013)
-- Name: rmt_plan_promotion_type fk_rmt_plan_prom_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_promotion_type
    ADD CONSTRAINT fk_rmt_plan_prom_t FOREIGN KEY (plan_promotion_type_id) REFERENCES tid_plan_promotion_type_id(plan_promotion_type_id) ON DELETE CASCADE;


--
-- TOC entry 2874 (class 2606 OID 20018)
-- Name: rmt_plan_promotion_type fk_rmt_plan_prom_t_discount_c; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_promotion_type
    ADD CONSTRAINT fk_rmt_plan_prom_t_discount_c FOREIGN KEY (discount_concept_id) REFERENCES mt_discount_concept(discount_concept_id);


--
-- TOC entry 2875 (class 2606 OID 20023)
-- Name: rmt_plan_promotion_type fk_rmt_plan_prom_t_discount_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_promotion_type
    ADD CONSTRAINT fk_rmt_plan_prom_t_discount_t FOREIGN KEY (discount_type_id) REFERENCES mt_discount_type(discount_type_id);


--
-- TOC entry 2876 (class 2606 OID 20028)
-- Name: rmt_plan_promotion_type fk_rmt_plan_prom_t_plan; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_promotion_type
    ADD CONSTRAINT fk_rmt_plan_prom_t_plan FOREIGN KEY (plan_type_id) REFERENCES tid_plan_type_id(plan_type_id);


--
-- TOC entry 2877 (class 2606 OID 20033)
-- Name: rmt_plan_promotion_type fk_rmt_plan_prom_t_promo; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_promotion_type
    ADD CONSTRAINT fk_rmt_plan_prom_t_promo FOREIGN KEY (promotion_type_id) REFERENCES tid_promotion_type_id(promotion_type_id);


--
-- TOC entry 2878 (class 2606 OID 20038)
-- Name: rmt_plan_promotion_type fk_rmt_plan_prom_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_plan_promotion_type
    ADD CONSTRAINT fk_rmt_plan_prom_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2879 (class 2606 OID 20043)
-- Name: rmt_promotion_equipment_type fk_rmt_prom_equip_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_equipment_type
    ADD CONSTRAINT fk_rmt_prom_equip_t FOREIGN KEY (prom_equip_type_id) REFERENCES tid_promotion_equipment_type_id(prom_equip_type_id) ON DELETE CASCADE;


--
-- TOC entry 2880 (class 2606 OID 20048)
-- Name: rmt_promotion_equipment_type fk_rmt_prom_equip_t_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_equipment_type
    ADD CONSTRAINT fk_rmt_prom_equip_t_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2881 (class 2606 OID 20053)
-- Name: rmt_promotion_equipment_type fk_rmt_prom_equip_t_equip; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_equipment_type
    ADD CONSTRAINT fk_rmt_prom_equip_t_equip FOREIGN KEY (equipment_type_id) REFERENCES tid_equipment_type_id(equipment_type_id);


--
-- TOC entry 2882 (class 2606 OID 20058)
-- Name: rmt_promotion_equipment_type fk_rmt_prom_equip_t_prom; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_equipment_type
    ADD CONSTRAINT fk_rmt_prom_equip_t_prom FOREIGN KEY (promotion_type_id) REFERENCES tid_promotion_type_id(promotion_type_id);


--
-- TOC entry 2883 (class 2606 OID 20063)
-- Name: rmt_promotion_equipment_type fk_rmt_prom_equip_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_equipment_type
    ADD CONSTRAINT fk_rmt_prom_equip_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2884 (class 2606 OID 20068)
-- Name: rmt_promotion_equipment_type fk_rmt_prom_equip_t_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_equipment_type
    ADD CONSTRAINT fk_rmt_prom_equip_t_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2885 (class 2606 OID 20073)
-- Name: rmt_promotion_product_type fk_rmt_prom_product_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_product_type
    ADD CONSTRAINT fk_rmt_prom_product_t FOREIGN KEY (prom_prod_type_id) REFERENCES tid_promotion_product_type_id(prom_prod_type_id) ON DELETE CASCADE;


--
-- TOC entry 2886 (class 2606 OID 20078)
-- Name: rmt_promotion_product_type fk_rmt_prom_product_t_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_product_type
    ADD CONSTRAINT fk_rmt_prom_product_t_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2887 (class 2606 OID 20083)
-- Name: rmt_promotion_product_type fk_rmt_prom_product_t_prod; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_product_type
    ADD CONSTRAINT fk_rmt_prom_product_t_prod FOREIGN KEY (product_type_id) REFERENCES tid_product_type_id(product_type_id);


--
-- TOC entry 2888 (class 2606 OID 20088)
-- Name: rmt_promotion_product_type fk_rmt_prom_product_t_prom; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_product_type
    ADD CONSTRAINT fk_rmt_prom_product_t_prom FOREIGN KEY (promotion_type_id) REFERENCES tid_promotion_type_id(promotion_type_id);


--
-- TOC entry 2889 (class 2606 OID 20093)
-- Name: rmt_promotion_product_type fk_rmt_prom_product_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_product_type
    ADD CONSTRAINT fk_rmt_prom_product_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2890 (class 2606 OID 20098)
-- Name: rmt_promotion_product_type fk_rmt_prom_product_t_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_product_type
    ADD CONSTRAINT fk_rmt_prom_product_t_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2891 (class 2606 OID 20103)
-- Name: rmt_promotion_service_type fk_rmt_prom_service_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_service_type
    ADD CONSTRAINT fk_rmt_prom_service_t FOREIGN KEY (prom_serv_type_id) REFERENCES tid_promotion_service_type_id(prom_serv_type_id) ON DELETE CASCADE;


--
-- TOC entry 2892 (class 2606 OID 20108)
-- Name: rmt_promotion_service_type fk_rmt_prom_service_t_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_service_type
    ADD CONSTRAINT fk_rmt_prom_service_t_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2893 (class 2606 OID 20113)
-- Name: rmt_promotion_service_type fk_rmt_prom_service_t_prom; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_service_type
    ADD CONSTRAINT fk_rmt_prom_service_t_prom FOREIGN KEY (promotion_type_id) REFERENCES tid_promotion_type_id(promotion_type_id);


--
-- TOC entry 2894 (class 2606 OID 20118)
-- Name: rmt_promotion_service_type fk_rmt_prom_service_t_serv; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_service_type
    ADD CONSTRAINT fk_rmt_prom_service_t_serv FOREIGN KEY (service_type_id) REFERENCES tid_service_type_id(service_type_id);


--
-- TOC entry 2895 (class 2606 OID 20123)
-- Name: rmt_promotion_service_type fk_rmt_prom_service_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_service_type
    ADD CONSTRAINT fk_rmt_prom_service_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2896 (class 2606 OID 20128)
-- Name: rmt_promotion_service_type fk_rmt_prom_service_t_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_promotion_service_type
    ADD CONSTRAINT fk_rmt_prom_service_t_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2897 (class 2606 OID 20133)
-- Name: rmt_service_equipment_type fk_rmt_serv_equip_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_service_equipment_type
    ADD CONSTRAINT fk_rmt_serv_equip_t FOREIGN KEY (service_equipment_type_id) REFERENCES tid_service_equipment_type_id(service_equipment_type_id) ON DELETE CASCADE;


--
-- TOC entry 2898 (class 2606 OID 20138)
-- Name: rmt_service_equipment_type fk_rmt_serv_equip_t_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_service_equipment_type
    ADD CONSTRAINT fk_rmt_serv_equip_t_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2899 (class 2606 OID 20143)
-- Name: rmt_service_equipment_type fk_rmt_serv_equip_t_equip; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_service_equipment_type
    ADD CONSTRAINT fk_rmt_serv_equip_t_equip FOREIGN KEY (equipment_type_id) REFERENCES tid_equipment_type_id(equipment_type_id);


--
-- TOC entry 2900 (class 2606 OID 20148)
-- Name: rmt_service_equipment_type fk_rmt_serv_equip_t_serv; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_service_equipment_type
    ADD CONSTRAINT fk_rmt_serv_equip_t_serv FOREIGN KEY (service_type_id) REFERENCES tid_service_type_id(service_type_id);


--
-- TOC entry 2901 (class 2606 OID 20153)
-- Name: rmt_service_equipment_type fk_rmt_serv_equip_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_service_equipment_type
    ADD CONSTRAINT fk_rmt_serv_equip_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2902 (class 2606 OID 20158)
-- Name: rmt_service_equipment_type fk_rmt_serv_equip_t_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_service_equipment_type
    ADD CONSTRAINT fk_rmt_serv_equip_t_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2903 (class 2606 OID 20163)
-- Name: rmt_tariff_equipment_type fk_rmt_tariff_equipment_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_equipment_type
    ADD CONSTRAINT fk_rmt_tariff_equipment_t FOREIGN KEY (tariff_equipment_type_id) REFERENCES tid_tariff_equipment_type_id(tariff_equipment_type_id) ON DELETE CASCADE;


--
-- TOC entry 2904 (class 2606 OID 20168)
-- Name: rmt_tariff_equipment_type fk_rmt_tariff_equipment_t_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_equipment_type
    ADD CONSTRAINT fk_rmt_tariff_equipment_t_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2905 (class 2606 OID 20173)
-- Name: rmt_tariff_equipment_type fk_rmt_tariff_equipment_t_equipment; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_equipment_type
    ADD CONSTRAINT fk_rmt_tariff_equipment_t_equipment FOREIGN KEY (equipment_type_id) REFERENCES tid_equipment_type_id(equipment_type_id);


--
-- TOC entry 2906 (class 2606 OID 20178)
-- Name: rmt_tariff_equipment_type fk_rmt_tariff_equipment_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_equipment_type
    ADD CONSTRAINT fk_rmt_tariff_equipment_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2907 (class 2606 OID 20183)
-- Name: rmt_tariff_equipment_type fk_rmt_tariff_equipment_t_tariff; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_equipment_type
    ADD CONSTRAINT fk_rmt_tariff_equipment_t_tariff FOREIGN KEY (tariff_type_id) REFERENCES tid_tariff_type_id(tariff_type_id);


--
-- TOC entry 2908 (class 2606 OID 20188)
-- Name: rmt_tariff_equipment_type fk_rmt_tariff_equipment_t_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_equipment_type
    ADD CONSTRAINT fk_rmt_tariff_equipment_t_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2909 (class 2606 OID 20193)
-- Name: rmt_tariff_product_type fk_rmt_tariff_product_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_product_type
    ADD CONSTRAINT fk_rmt_tariff_product_t FOREIGN KEY (tariff_product_type_id) REFERENCES tid_tariff_product_type_id(tariff_product_type_id) ON DELETE CASCADE;


--
-- TOC entry 2910 (class 2606 OID 20198)
-- Name: rmt_tariff_product_type fk_rmt_tariff_product_t_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_product_type
    ADD CONSTRAINT fk_rmt_tariff_product_t_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2911 (class 2606 OID 20203)
-- Name: rmt_tariff_product_type fk_rmt_tariff_product_t_product; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_product_type
    ADD CONSTRAINT fk_rmt_tariff_product_t_product FOREIGN KEY (product_type_id) REFERENCES tid_product_type_id(product_type_id);


--
-- TOC entry 2912 (class 2606 OID 20208)
-- Name: rmt_tariff_product_type fk_rmt_tariff_product_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_product_type
    ADD CONSTRAINT fk_rmt_tariff_product_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2913 (class 2606 OID 20213)
-- Name: rmt_tariff_product_type fk_rmt_tariff_product_t_tariff; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_product_type
    ADD CONSTRAINT fk_rmt_tariff_product_t_tariff FOREIGN KEY (tariff_type_id) REFERENCES tid_tariff_type_id(tariff_type_id);


--
-- TOC entry 2914 (class 2606 OID 20218)
-- Name: rmt_tariff_product_type fk_rmt_tariff_product_t_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_product_type
    ADD CONSTRAINT fk_rmt_tariff_product_t_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2915 (class 2606 OID 20223)
-- Name: rmt_tariff_promotion_type fk_rmt_tariff_prom_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_promotion_type
    ADD CONSTRAINT fk_rmt_tariff_prom_t FOREIGN KEY (tariff_promotion_type_id) REFERENCES tid_tariff_promotion_type_id(tariff_promotion_type_id) ON DELETE CASCADE;


--
-- TOC entry 2916 (class 2606 OID 20228)
-- Name: rmt_tariff_promotion_type fk_rmt_tariff_prom_t_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_promotion_type
    ADD CONSTRAINT fk_rmt_tariff_prom_t_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2917 (class 2606 OID 20233)
-- Name: rmt_tariff_promotion_type fk_rmt_tariff_prom_t_prom; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_promotion_type
    ADD CONSTRAINT fk_rmt_tariff_prom_t_prom FOREIGN KEY (promotion_type_id) REFERENCES tid_promotion_type_id(promotion_type_id);


--
-- TOC entry 2918 (class 2606 OID 20238)
-- Name: rmt_tariff_promotion_type fk_rmt_tariff_prom_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_promotion_type
    ADD CONSTRAINT fk_rmt_tariff_prom_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2919 (class 2606 OID 20243)
-- Name: rmt_tariff_promotion_type fk_rmt_tariff_prom_t_tariff; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_promotion_type
    ADD CONSTRAINT fk_rmt_tariff_prom_t_tariff FOREIGN KEY (tariff_type_id) REFERENCES tid_tariff_type_id(tariff_type_id);


--
-- TOC entry 2920 (class 2606 OID 20248)
-- Name: rmt_tariff_promotion_type fk_rmt_tariff_prom_t_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_promotion_type
    ADD CONSTRAINT fk_rmt_tariff_prom_t_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 2921 (class 2606 OID 20253)
-- Name: rmt_tariff_service_type fk_rmt_tariff_service_t; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_service_type
    ADD CONSTRAINT fk_rmt_tariff_service_t FOREIGN KEY (tariff_service_type_id) REFERENCES tid_tariff_service_type_id(tariff_service_type_id) ON DELETE CASCADE;


--
-- TOC entry 2922 (class 2606 OID 20258)
-- Name: rmt_tariff_service_type fk_rmt_tariff_service_t_business; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_service_type
    ADD CONSTRAINT fk_rmt_tariff_service_t_business FOREIGN KEY (business_scope_id) REFERENCES mt_business_scope(business_scope_id);


--
-- TOC entry 2923 (class 2606 OID 20263)
-- Name: rmt_tariff_service_type fk_rmt_tariff_service_t_service; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_service_type
    ADD CONSTRAINT fk_rmt_tariff_service_t_service FOREIGN KEY (service_type_id) REFERENCES tid_service_type_id(service_type_id);


--
-- TOC entry 2924 (class 2606 OID 20268)
-- Name: rmt_tariff_service_type fk_rmt_tariff_service_t_status; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_service_type
    ADD CONSTRAINT fk_rmt_tariff_service_t_status FOREIGN KEY (status_id) REFERENCES mt_status(status_id);


--
-- TOC entry 2925 (class 2606 OID 20273)
-- Name: rmt_tariff_service_type fk_rmt_tariff_service_t_tariff; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_service_type
    ADD CONSTRAINT fk_rmt_tariff_service_t_tariff FOREIGN KEY (tariff_type_id) REFERENCES tid_tariff_type_id(tariff_type_id);


--
-- TOC entry 2926 (class 2606 OID 20278)
-- Name: rmt_tariff_service_type fk_rmt_tariff_service_t_technol; Type: FK CONSTRAINT; Schema: billing; Owner: billing_admin
--

ALTER TABLE ONLY rmt_tariff_service_type
    ADD CONSTRAINT fk_rmt_tariff_service_t_technol FOREIGN KEY (technology_scope_id) REFERENCES mt_technology_scope(technology_scope_id);


--
-- TOC entry 3163 (class 0 OID 0)
-- Dependencies: 5
-- Name: billing; Type: ACL; Schema: -; Owner: billing_admin
--

GRANT USAGE ON SCHEMA billing TO billing_appl;


SET SESSION AUTHORIZATION 'postgres';

--
-- TOC entry 3165 (class 0 OID 0)
-- Dependencies: 9
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

GRANT ALL ON SCHEMA public TO PUBLIC;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3168 (class 0 OID 0)
-- Dependencies: 333
-- Name: tf_add_product_type_validation(); Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT ALL ON FUNCTION tf_add_product_type_validation() TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3169 (class 0 OID 0)
-- Dependencies: 334
-- Name: tf_add_service_type_validation(); Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT ALL ON FUNCTION tf_add_service_type_validation() TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3170 (class 0 OID 0)
-- Dependencies: 335
-- Name: tf_add_user_validation(); Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT ALL ON FUNCTION tf_add_user_validation() TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3171 (class 0 OID 0)
-- Dependencies: 330
-- Name: tf_format_default_dates(); Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT ALL ON FUNCTION tf_format_default_dates() TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3172 (class 0 OID 0)
-- Dependencies: 329
-- Name: tf_product_type_align_records(timestamp without time zone, timestamp without time zone, integer, character varying, timestamp without time zone); Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT ALL ON FUNCTION tf_product_type_align_records(i_start_date timestamp without time zone, i_end_date timestamp without time zone, i_product_type_id integer, i_modif_user character varying, i_modif_date timestamp without time zone) TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3173 (class 0 OID 0)
-- Dependencies: 336
-- Name: tf_status_entity_type_validation(); Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT ALL ON FUNCTION tf_status_entity_type_validation() TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3180 (class 0 OID 0)
-- Dependencies: 187
-- Name: it_user; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE it_user TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3185 (class 0 OID 0)
-- Dependencies: 188
-- Name: mt_application_level; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_application_level TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3187 (class 0 OID 0)
-- Dependencies: 189
-- Name: mt_application_level_application_level_id_seq; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,USAGE ON SEQUENCE mt_application_level_application_level_id_seq TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3192 (class 0 OID 0)
-- Dependencies: 190
-- Name: mt_application_unit; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_application_unit TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3194 (class 0 OID 0)
-- Dependencies: 191
-- Name: mt_application_unit_application_unit_id_seq; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,USAGE ON SEQUENCE mt_application_unit_application_unit_id_seq TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3199 (class 0 OID 0)
-- Dependencies: 192
-- Name: mt_business_scope; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_business_scope TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3201 (class 0 OID 0)
-- Dependencies: 193
-- Name: mt_business_scope_business_scope_id_seq; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,USAGE ON SEQUENCE mt_business_scope_business_scope_id_seq TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3216 (class 0 OID 0)
-- Dependencies: 194
-- Name: mt_consumption_code; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_consumption_code TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3221 (class 0 OID 0)
-- Dependencies: 195
-- Name: mt_consumption_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_consumption_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3223 (class 0 OID 0)
-- Dependencies: 196
-- Name: mt_consumption_type_consumption_type_id_seq; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,USAGE ON SEQUENCE mt_consumption_type_consumption_type_id_seq TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3228 (class 0 OID 0)
-- Dependencies: 197
-- Name: mt_discount_concept; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_discount_concept TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3230 (class 0 OID 0)
-- Dependencies: 198
-- Name: mt_discount_concept_discount_concept_id_seq; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,USAGE ON SEQUENCE mt_discount_concept_discount_concept_id_seq TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3235 (class 0 OID 0)
-- Dependencies: 199
-- Name: mt_discount_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_discount_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3237 (class 0 OID 0)
-- Dependencies: 200
-- Name: mt_discount_type_discount_type_id_seq; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,USAGE ON SEQUENCE mt_discount_type_discount_type_id_seq TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3242 (class 0 OID 0)
-- Dependencies: 201
-- Name: mt_entity_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_entity_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3244 (class 0 OID 0)
-- Dependencies: 202
-- Name: mt_entity_type_entity_type_id_seq; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,USAGE ON SEQUENCE mt_entity_type_entity_type_id_seq TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3258 (class 0 OID 0)
-- Dependencies: 203
-- Name: mt_equipment_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_equipment_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3276 (class 0 OID 0)
-- Dependencies: 204
-- Name: mt_fee_code; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_fee_code TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3291 (class 0 OID 0)
-- Dependencies: 205
-- Name: mt_plan_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_plan_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3306 (class 0 OID 0)
-- Dependencies: 206
-- Name: mt_product_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_product_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3317 (class 0 OID 0)
-- Dependencies: 207
-- Name: mt_profile; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_profile TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3333 (class 0 OID 0)
-- Dependencies: 208
-- Name: mt_promotion_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_promotion_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3338 (class 0 OID 0)
-- Dependencies: 209
-- Name: mt_recurrence; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_recurrence TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3340 (class 0 OID 0)
-- Dependencies: 210
-- Name: mt_recurrence_recurrence_id_seq; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,USAGE ON SEQUENCE mt_recurrence_recurrence_id_seq TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3355 (class 0 OID 0)
-- Dependencies: 211
-- Name: mt_service_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_service_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3361 (class 0 OID 0)
-- Dependencies: 212
-- Name: mt_status; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_status TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3363 (class 0 OID 0)
-- Dependencies: 213
-- Name: mt_status_status_id_seq; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,USAGE ON SEQUENCE mt_status_status_id_seq TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3380 (class 0 OID 0)
-- Dependencies: 214
-- Name: mt_tariff_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_tariff_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3385 (class 0 OID 0)
-- Dependencies: 215
-- Name: mt_technology_scope; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE mt_technology_scope TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3387 (class 0 OID 0)
-- Dependencies: 216
-- Name: mt_technology_scope_technology_scope_id_seq; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,USAGE ON SEQUENCE mt_technology_scope_technology_scope_id_seq TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3401 (class 0 OID 0)
-- Dependencies: 217
-- Name: rmt_fee_equip_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_fee_equip_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3415 (class 0 OID 0)
-- Dependencies: 218
-- Name: rmt_fee_product_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_fee_product_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3429 (class 0 OID 0)
-- Dependencies: 219
-- Name: rmt_fee_promotion_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_fee_promotion_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3443 (class 0 OID 0)
-- Dependencies: 220
-- Name: rmt_fee_service_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_fee_service_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3454 (class 0 OID 0)
-- Dependencies: 221
-- Name: rmt_plan_charge_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_plan_charge_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3471 (class 0 OID 0)
-- Dependencies: 222
-- Name: rmt_plan_discount_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_plan_discount_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3484 (class 0 OID 0)
-- Dependencies: 223
-- Name: rmt_plan_prerreq_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_plan_prerreq_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3499 (class 0 OID 0)
-- Dependencies: 224
-- Name: rmt_plan_promotion_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_plan_promotion_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3512 (class 0 OID 0)
-- Dependencies: 225
-- Name: rmt_promotion_equipment_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_promotion_equipment_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3526 (class 0 OID 0)
-- Dependencies: 226
-- Name: rmt_promotion_product_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_promotion_product_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3539 (class 0 OID 0)
-- Dependencies: 227
-- Name: rmt_promotion_service_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_promotion_service_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3553 (class 0 OID 0)
-- Dependencies: 228
-- Name: rmt_service_equipment_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_service_equipment_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3567 (class 0 OID 0)
-- Dependencies: 229
-- Name: rmt_tariff_equipment_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_tariff_equipment_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3581 (class 0 OID 0)
-- Dependencies: 230
-- Name: rmt_tariff_product_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_tariff_product_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3595 (class 0 OID 0)
-- Dependencies: 231
-- Name: rmt_tariff_promotion_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_tariff_promotion_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3609 (class 0 OID 0)
-- Dependencies: 232
-- Name: rmt_tariff_service_type; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE rmt_tariff_service_type TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3610 (class 0 OID 0)
-- Dependencies: 233
-- Name: test_menu; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE test_menu TO billing_appl;
GRANT ALL ON TABLE test_menu TO postgres;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3611 (class 0 OID 0)
-- Dependencies: 234
-- Name: test_user; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE test_user TO billing_appl;
GRANT ALL ON TABLE test_user TO postgres;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3614 (class 0 OID 0)
-- Dependencies: 235
-- Name: tid_consumption_code_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_consumption_code_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3618 (class 0 OID 0)
-- Dependencies: 237
-- Name: tid_equipment_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_equipment_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3622 (class 0 OID 0)
-- Dependencies: 239
-- Name: tid_fee_code_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_fee_code_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3626 (class 0 OID 0)
-- Dependencies: 241
-- Name: tid_fee_equipment_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_fee_equipment_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3630 (class 0 OID 0)
-- Dependencies: 243
-- Name: tid_fee_product_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_fee_product_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3634 (class 0 OID 0)
-- Dependencies: 245
-- Name: tid_fee_promotion_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_fee_promotion_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3638 (class 0 OID 0)
-- Dependencies: 247
-- Name: tid_fee_service_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_fee_service_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3642 (class 0 OID 0)
-- Dependencies: 249
-- Name: tid_plan_charge_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_plan_charge_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3646 (class 0 OID 0)
-- Dependencies: 251
-- Name: tid_plan_discount_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_plan_discount_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3650 (class 0 OID 0)
-- Dependencies: 253
-- Name: tid_plan_prerreq_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_plan_prerreq_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3654 (class 0 OID 0)
-- Dependencies: 255
-- Name: tid_plan_promotion_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_plan_promotion_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3658 (class 0 OID 0)
-- Dependencies: 257
-- Name: tid_plan_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_plan_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3662 (class 0 OID 0)
-- Dependencies: 259
-- Name: tid_product_service_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_product_service_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3666 (class 0 OID 0)
-- Dependencies: 261
-- Name: tid_product_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_product_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3668 (class 0 OID 0)
-- Dependencies: 262
-- Name: tid_product_type_id_product_type_id_seq; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,USAGE ON SEQUENCE tid_product_type_id_product_type_id_seq TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3671 (class 0 OID 0)
-- Dependencies: 263
-- Name: tid_profile_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_profile_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3673 (class 0 OID 0)
-- Dependencies: 264
-- Name: tid_profile_id_profile_id_seq; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT ON SEQUENCE tid_profile_id_profile_id_seq TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3676 (class 0 OID 0)
-- Dependencies: 265
-- Name: tid_promotion_equipment_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_promotion_equipment_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3680 (class 0 OID 0)
-- Dependencies: 267
-- Name: tid_promotion_product_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_promotion_product_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3684 (class 0 OID 0)
-- Dependencies: 269
-- Name: tid_promotion_promotion_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_promotion_promotion_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3688 (class 0 OID 0)
-- Dependencies: 271
-- Name: tid_promotion_service_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_promotion_service_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3692 (class 0 OID 0)
-- Dependencies: 273
-- Name: tid_promotion_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_promotion_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3696 (class 0 OID 0)
-- Dependencies: 275
-- Name: tid_service_equipment_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_service_equipment_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3700 (class 0 OID 0)
-- Dependencies: 277
-- Name: tid_service_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_service_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3702 (class 0 OID 0)
-- Dependencies: 278
-- Name: tid_service_type_id_service_type_id_seq; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,USAGE ON SEQUENCE tid_service_type_id_service_type_id_seq TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3705 (class 0 OID 0)
-- Dependencies: 279
-- Name: tid_tariff_equipment_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_tariff_equipment_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3709 (class 0 OID 0)
-- Dependencies: 281
-- Name: tid_tariff_product_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_tariff_product_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3713 (class 0 OID 0)
-- Dependencies: 283
-- Name: tid_tariff_promotion_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_tariff_promotion_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3717 (class 0 OID 0)
-- Dependencies: 285
-- Name: tid_tariff_service_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_tariff_service_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3721 (class 0 OID 0)
-- Dependencies: 287
-- Name: tid_tariff_type_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_tariff_type_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3725 (class 0 OID 0)
-- Dependencies: 289
-- Name: tid_user_id; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,TRUNCATE,UPDATE ON TABLE tid_user_id TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3727 (class 0 OID 0)
-- Dependencies: 290
-- Name: tid_user_id_user_id_seq; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,USAGE ON SEQUENCE tid_user_id_user_id_seq TO billing_appl;


SET SESSION AUTHORIZATION 'billing_admin';

--
-- TOC entry 3729 (class 0 OID 0)
-- Dependencies: 291
-- Name: v_user_profile; Type: ACL; Schema: billing; Owner: billing_admin
--

GRANT SELECT,INSERT,DELETE,UPDATE ON TABLE v_user_profile TO billing_appl;
GRANT ALL ON TABLE v_user_profile TO postgres;


-- Completed on 2017-11-01 14:51:49 CET

--
-- PostgreSQL database dump complete
--

