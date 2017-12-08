--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.5
-- Dumped by pg_dump version 9.6.5

-- Started on 2017-11-01 15:01:05 CET

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET SESSION AUTHORIZATION 'billing_admin';

SET search_path = billing, pg_catalog;

--
-- TOC entry 3054 (class 0 OID 18998)
-- Dependencies: 187
-- Data for Name: it_user; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO it_user (user_id, user_code, user_name, password, profile_id, start_date, end_date, status_id, input_date, input_user, modif_date, modif_user) VALUES (2, 'DBADMIN', 'Base DBA Admin', 'e4a7bd7e5ace5690baab7972a9455efc', 2, '2016-06-30 00:00:00', '9999-12-31 23:59:59', 1, '2016-06-30 20:04:17', 'SYSTEM', NULL, NULL);
INSERT INTO it_user (user_id, user_code, user_name, password, profile_id, start_date, end_date, status_id, input_date, input_user, modif_date, modif_user) VALUES (3, 'USER', 'Base User', '2e40ad879e955201df4dedbf8d479a12', 3, '2016-06-30 00:00:00', '9999-12-31 23:59:59', 1, '2016-06-30 20:04:48', 'SYSTEM', NULL, NULL);
INSERT INTO it_user (user_id, user_code, user_name, password, profile_id, start_date, end_date, status_id, input_date, input_user, modif_date, modif_user) VALUES (4, 'USER1', 'User N1', '9f693771ca12c43759045cdf4295e9f5', 3, '2016-06-30 00:00:00', '9999-12-31 23:59:59', 1, '2016-06-30 20:05:26', 'SYSTEM', NULL, NULL);
INSERT INTO it_user (user_id, user_code, user_name, password, profile_id, start_date, end_date, status_id, input_date, input_user, modif_date, modif_user) VALUES (13, '3', '3', 'eccbc87e4b5ce2fe28308fd9f2a7baf3', 3, '1900-01-01 00:00:00', '9999-12-31 23:59:59', 1, '2016-10-29 16:07:54.211', 'USER', NULL, NULL);
INSERT INTO it_user (user_id, user_code, user_name, password, profile_id, start_date, end_date, status_id, input_date, input_user, modif_date, modif_user) VALUES (7, '1', '1', 'c4ca4238a0b923820dcc509a6f75849b', 1, '1900-01-01 00:00:00', '9999-12-31 23:59:59', 1, '2016-10-22 12:35:23.268', 'USER', NULL, NULL);
INSERT INTO it_user (user_id, user_code, user_name, password, profile_id, start_date, end_date, status_id, input_date, input_user, modif_date, modif_user) VALUES (5, 'TEST', 'TEST USER', '033bd94b1168d7e4f0d644c3c95e35bf', 3, '1900-01-01 00:00:00', '2017-04-30 23:59:59', 1, '2016-09-29 19:21:04.832', 'USER', '2017-05-27 12:29:57.136', 'USER');
INSERT INTO it_user (user_id, user_code, user_name, password, profile_id, start_date, end_date, status_id, input_date, input_user, modif_date, modif_user) VALUES (5, 'TEST', 'TEST USER', '033bd94b1168d7e4f0d644c3c95e35bf', 3, '2017-05-01 00:00:00', '9999-12-31 23:59:59', 1, '2017-05-27 12:29:57.136', 'USER', NULL, NULL);
INSERT INTO it_user (user_id, user_code, user_name, password, profile_id, start_date, end_date, status_id, input_date, input_user, modif_date, modif_user) VALUES (9, '2', '2', 'b6d767d2f8ed5d21a44b0e5886680cb9', 3, '1900-01-01 00:00:00', '2020-07-31 23:59:59', 1, '2016-10-29 12:24:40.888', 'USER', '2017-05-30 21:13:28.935', 'USER');
INSERT INTO it_user (user_id, user_code, user_name, password, profile_id, start_date, end_date, status_id, input_date, input_user, modif_date, modif_user) VALUES (9, '2', '2', 'b6d767d2f8ed5d21a44b0e5886680cb9', 3, '2020-08-01 00:00:00', '9999-12-31 23:59:59', 1, '2017-05-30 21:13:28.935', 'USER', '2017-05-30 21:14:10.673', 'USER');
INSERT INTO it_user (user_id, user_code, user_name, password, profile_id, start_date, end_date, status_id, input_date, input_user, modif_date, modif_user) VALUES (114, 'ADMIN', 'ADMIN', '73acd9a5972130b75066c82595a1fae3', 1, '1900-01-01 00:00:00', '9999-12-31 23:59:59', 1, '2017-07-12 18:04:29', 'USER', NULL, NULL);


--
-- TOC entry 3055 (class 0 OID 19004)
-- Dependencies: 188
-- Data for Name: mt_application_level; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO mt_application_level (application_level_id, application_level_code, description) VALUES (3, 'PROD', 'Product level');
INSERT INTO mt_application_level (application_level_id, application_level_code, description) VALUES (9, 'SERV', 'Service level');
INSERT INTO mt_application_level (application_level_id, application_level_code, description) VALUES (10, 'EQUI', 'Equipment level');
INSERT INTO mt_application_level (application_level_id, application_level_code, description) VALUES (14, 'PROM', 'Promotion level');


--
-- TOC entry 3729 (class 0 OID 0)
-- Dependencies: 189
-- Name: mt_application_level_application_level_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_application_level_application_level_id_seq', 16, true);


--
-- TOC entry 3057 (class 0 OID 19009)
-- Dependencies: 190
-- Data for Name: mt_application_unit; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO mt_application_unit (application_unit_id, application_unit_code, description) VALUES (3, 'EUR', 'Discount in euros');
INSERT INTO mt_application_unit (application_unit_id, application_unit_code, description) VALUES (2, 'UNIT', 'Discount in units');


--
-- TOC entry 3730 (class 0 OID 0)
-- Dependencies: 191
-- Name: mt_application_unit_application_unit_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_application_unit_application_unit_id_seq', 3, true);


--
-- TOC entry 3059 (class 0 OID 19014)
-- Dependencies: 192
-- Data for Name: mt_business_scope; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO mt_business_scope (business_scope_id, business_scope_code, description) VALUES (1, 'CORP', 'Corporate scope');
INSERT INTO mt_business_scope (business_scope_id, business_scope_code, description) VALUES (2, 'PERS', 'Personal scope');
INSERT INTO mt_business_scope (business_scope_id, business_scope_code, description) VALUES (3, 'IND', 'Indiferent scope');


--
-- TOC entry 3731 (class 0 OID 0)
-- Dependencies: 193
-- Name: mt_business_scope_business_scope_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_business_scope_business_scope_id_seq', 3, true);


--
-- TOC entry 3061 (class 0 OID 19019)
-- Dependencies: 194
-- Data for Name: mt_consumption_code; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3062 (class 0 OID 19025)
-- Dependencies: 195
-- Data for Name: mt_consumption_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO mt_consumption_type (consumption_type_id, consumption_type_code, description) VALUES (2, 'FCALL', 'Fixed phone call');
INSERT INTO mt_consumption_type (consumption_type_id, consumption_type_code, description) VALUES (3, 'MCALL', 'Mobile phone call');


--
-- TOC entry 3732 (class 0 OID 0)
-- Dependencies: 196
-- Name: mt_consumption_type_consumption_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_consumption_type_consumption_type_id_seq', 3, true);


--
-- TOC entry 3064 (class 0 OID 19030)
-- Dependencies: 197
-- Data for Name: mt_discount_concept; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO mt_discount_concept (discount_concept_id, discount_concept_code, description) VALUES (1, 'FEE', 'Fee discount');
INSERT INTO mt_discount_concept (discount_concept_id, discount_concept_code, description) VALUES (3, 'FPH', 'Fixed phone discount');
INSERT INTO mt_discount_concept (discount_concept_id, discount_concept_code, description) VALUES (4, 'MOB', 'Mobile phone discount');


--
-- TOC entry 3733 (class 0 OID 0)
-- Dependencies: 198
-- Name: mt_discount_concept_discount_concept_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_discount_concept_discount_concept_id_seq', 4, true);


--
-- TOC entry 3066 (class 0 OID 19035)
-- Dependencies: 199
-- Data for Name: mt_discount_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO mt_discount_type (discount_type_id, discount_type_code, description) VALUES (1, 'FIXED', 'Fixed discount');
INSERT INTO mt_discount_type (discount_type_id, discount_type_code, description) VALUES (2, 'VARPER', 'Variable percentaje discount');
INSERT INTO mt_discount_type (discount_type_id, discount_type_code, description) VALUES (8, 'VARAMO', 'Variable amount discount');
INSERT INTO mt_discount_type (discount_type_id, discount_type_code, description) VALUES (14, 'TRES', 'TRES');


--
-- TOC entry 3734 (class 0 OID 0)
-- Dependencies: 200
-- Name: mt_discount_type_discount_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_discount_type_discount_type_id_seq', 14, true);


--
-- TOC entry 3068 (class 0 OID 19040)
-- Dependencies: 201
-- Data for Name: mt_entity_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO mt_entity_type (entity_type_id, entity_type_code, description) VALUES (2, 'PROD', 'Product entity');
INSERT INTO mt_entity_type (entity_type_id, entity_type_code, description) VALUES (1, 'USER', 'User entity');
INSERT INTO mt_entity_type (entity_type_id, entity_type_code, description) VALUES (4, 'EQUIP', 'Equipment entity');
INSERT INTO mt_entity_type (entity_type_id, entity_type_code, description) VALUES (3, 'SERV', 'Service entity');
INSERT INTO mt_entity_type (entity_type_id, entity_type_code, description) VALUES (5, 'PROM', 'Promotion entity');
INSERT INTO mt_entity_type (entity_type_id, entity_type_code, description) VALUES (6, 'CONS', 'Consumption entity');


--
-- TOC entry 3735 (class 0 OID 0)
-- Dependencies: 202
-- Name: mt_entity_type_entity_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_entity_type_entity_type_id_seq', 6, true);


--
-- TOC entry 3070 (class 0 OID 19045)
-- Dependencies: 203
-- Data for Name: mt_equipment_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3071 (class 0 OID 19051)
-- Dependencies: 204
-- Data for Name: mt_fee_code; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3072 (class 0 OID 19059)
-- Dependencies: 205
-- Data for Name: mt_plan_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3073 (class 0 OID 19065)
-- Dependencies: 206
-- Data for Name: mt_product_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO mt_product_type (product_type_id, product_type_code, description, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user, entity_type_id) VALUES (4, 'PROD 04', 'Fourth product type', '1900-01-01 00:00:00', '9999-12-31 23:59:59', 3, 1, 1, '2017-09-06 20:48:31.876', 'ADMIN', NULL, NULL, 2);
INSERT INTO mt_product_type (product_type_id, product_type_code, description, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user, entity_type_id) VALUES (2, 'PROD 02', 'Second product type', '1900-01-01 00:00:00', '9999-12-31 23:59:59', 3, 2, 2, '2017-08-31 13:22:59', 'ADMIN', '2017-09-06 20:49:44', 'ADMIN', 2);
INSERT INTO mt_product_type (product_type_id, product_type_code, description, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user, entity_type_id) VALUES (3, 'PROD 03', 'Third product type', '1900-01-01 00:00:00', '9999-12-31 23:59:59', 3, 1, 1, '2017-09-03 13:49:21', 'ADMIN', '2017-09-06 20:50:40', 'ADMIN', 2);
INSERT INTO mt_product_type (product_type_id, product_type_code, description, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user, entity_type_id) VALUES (1, 'PROD 01', 'First product of the catalog', '1900-01-01 00:00:00', '1999-12-31 23:59:59', 3, 3, 1, '2017-08-24 19:03:17', 'ADMIN', '2017-10-24 20:32:37', 'ADMIN', 2);
INSERT INTO mt_product_type (product_type_id, product_type_code, description, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user, entity_type_id) VALUES (1, 'PROD 1', 'First product of the catalog', '2000-01-01 00:00:00', '9999-12-31 23:59:59', 3, 3, 1, '2017-09-07 21:16:54', 'ADMIN', '2017-09-06 21:24:05', 'ADMIN', 2);


--
-- TOC entry 3074 (class 0 OID 19072)
-- Dependencies: 207
-- Data for Name: mt_profile; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO mt_profile (profile_id, profile_code, description, start_date, end_date, input_date, input_user, modif_date, modif_user) VALUES (1, 'ADMIN', 'application admin', '2016-06-30 19:20:48', '9999-12-31 23:59:59', '2016-06-30 19:20:48', 'SYSTEM', NULL, NULL);
INSERT INTO mt_profile (profile_id, profile_code, description, start_date, end_date, input_date, input_user, modif_date, modif_user) VALUES (2, 'DBADMIN', 'db admin', '2016-06-30 19:21:14', '9999-12-31 23:59:59', '2016-06-30 19:21:14', 'SYSTEM', NULL, NULL);
INSERT INTO mt_profile (profile_id, profile_code, description, start_date, end_date, input_date, input_user, modif_date, modif_user) VALUES (3, 'USER', 'user', '2016-06-30 19:21:32', '9999-12-31 23:59:59', '2016-06-30 19:21:32', 'SYSTEM', NULL, NULL);


--
-- TOC entry 3075 (class 0 OID 19078)
-- Dependencies: 208
-- Data for Name: mt_promotion_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3076 (class 0 OID 19085)
-- Dependencies: 209
-- Data for Name: mt_recurrence; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO mt_recurrence (recurrence_id, recurrence_code, description) VALUES (1, 'ONEOFF', 'One-off');
INSERT INTO mt_recurrence (recurrence_id, recurrence_code, description) VALUES (2, 'INPUT', 'Input');
INSERT INTO mt_recurrence (recurrence_id, recurrence_code, description) VALUES (3, 'MONTH', 'Monthly');
INSERT INTO mt_recurrence (recurrence_id, recurrence_code, description) VALUES (4, 'ANNUAL', 'Annual');


--
-- TOC entry 3736 (class 0 OID 0)
-- Dependencies: 210
-- Name: mt_recurrence_recurrence_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_recurrence_recurrence_id_seq', 4, true);


--
-- TOC entry 3078 (class 0 OID 19090)
-- Dependencies: 211
-- Data for Name: mt_service_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO mt_service_type (service_type_id, service_type_code, description, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user, entity_type_id) VALUES (1, 'SERV 01', 'First service type', '1900-01-01 00:00:00', '9999-12-31 23:59:59', 7, 1, 1, '2017-08-31 13:21:02', 'ADMIN', '2017-09-06 22:41:15', 'ADMIN', 3);
INSERT INTO mt_service_type (service_type_id, service_type_code, description, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user, entity_type_id) VALUES (2, 'SERV 02', 'Second servyce type', '1900-01-01 00:00:00', '9999-12-31 23:59:59', 7, 2, 2, '2017-08-31 13:22:07', 'ADMIN', '2017-09-06 22:41:38', 'ADMIN', 3);
INSERT INTO mt_service_type (service_type_id, service_type_code, description, start_date, end_date, status_id, business_scope_id, technology_scope_id, input_date, input_user, modif_date, modif_user, entity_type_id) VALUES (3, 'SERV 03', 'Third service type', '1900-01-01 00:00:00', '9999-12-31 23:59:59', 7, 2, 2, '2017-08-31 13:34:27', 'ADMIN', '2017-09-06 22:42:03', 'ADMIN', 3);


--
-- TOC entry 3079 (class 0 OID 19097)
-- Dependencies: 212
-- Data for Name: mt_status; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO mt_status (status_id, entity_type_id, status_code, description) VALUES (1, 1, 'ACTIVE', 'user active');
INSERT INTO mt_status (status_id, entity_type_id, status_code, description) VALUES (2, 1, 'CANCEL', 'user cancelled');
INSERT INTO mt_status (status_id, entity_type_id, status_code, description) VALUES (7, 3, 'ACTIVE', 'Active service type status');
INSERT INTO mt_status (status_id, entity_type_id, status_code, description) VALUES (8, 3, 'CANCEL', 'Cancel service type status');
INSERT INTO mt_status (status_id, entity_type_id, status_code, description) VALUES (9, 3, 'DEBTOR', 'Debtor service type status');
INSERT INTO mt_status (status_id, entity_type_id, status_code, description) VALUES (10, 3, 'SUSP', 'Suspended service type status');
INSERT INTO mt_status (status_id, entity_type_id, status_code, description) VALUES (3, 2, 'ACTIVE', 'Active product type status');
INSERT INTO mt_status (status_id, entity_type_id, status_code, description) VALUES (4, 2, 'CANCEL', 'Cancel product type status');
INSERT INTO mt_status (status_id, entity_type_id, status_code, description) VALUES (6, 2, 'DEBTOR', 'Debtor product type status');
INSERT INTO mt_status (status_id, entity_type_id, status_code, description) VALUES (5, 2, 'SUSP', 'Suspended product type status');


--
-- TOC entry 3737 (class 0 OID 0)
-- Dependencies: 213
-- Name: mt_status_status_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_status_status_id_seq', 10, true);


--
-- TOC entry 3081 (class 0 OID 19102)
-- Dependencies: 214
-- Data for Name: mt_tariff_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3082 (class 0 OID 19109)
-- Dependencies: 215
-- Data for Name: mt_technology_scope; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO mt_technology_scope (technology_scope_id, technology_scope_code, description) VALUES (1, 'FIX', 'Fixed phone');
INSERT INTO mt_technology_scope (technology_scope_id, technology_scope_code, description) VALUES (2, 'MOB', 'Mobile phone');
INSERT INTO mt_technology_scope (technology_scope_id, technology_scope_code, description) VALUES (3, 'BTV', 'Basic television');


--
-- TOC entry 3738 (class 0 OID 0)
-- Dependencies: 216
-- Name: mt_technology_scope_technology_scope_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('mt_technology_scope_technology_scope_id_seq', 3, true);


--
-- TOC entry 3084 (class 0 OID 19114)
-- Dependencies: 217
-- Data for Name: rmt_fee_equip_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3085 (class 0 OID 19120)
-- Dependencies: 218
-- Data for Name: rmt_fee_product_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3086 (class 0 OID 19126)
-- Dependencies: 219
-- Data for Name: rmt_fee_promotion_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3087 (class 0 OID 19132)
-- Dependencies: 220
-- Data for Name: rmt_fee_service_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3088 (class 0 OID 19138)
-- Dependencies: 221
-- Data for Name: rmt_plan_charge_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3089 (class 0 OID 19144)
-- Dependencies: 222
-- Data for Name: rmt_plan_discount_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3090 (class 0 OID 19155)
-- Dependencies: 223
-- Data for Name: rmt_plan_prerreq_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3091 (class 0 OID 19161)
-- Dependencies: 224
-- Data for Name: rmt_plan_promotion_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3092 (class 0 OID 19168)
-- Dependencies: 225
-- Data for Name: rmt_promotion_equipment_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3093 (class 0 OID 19174)
-- Dependencies: 226
-- Data for Name: rmt_promotion_product_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3094 (class 0 OID 19180)
-- Dependencies: 227
-- Data for Name: rmt_promotion_service_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3095 (class 0 OID 19186)
-- Dependencies: 228
-- Data for Name: rmt_service_equipment_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3096 (class 0 OID 19192)
-- Dependencies: 229
-- Data for Name: rmt_tariff_equipment_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3097 (class 0 OID 19198)
-- Dependencies: 230
-- Data for Name: rmt_tariff_product_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3098 (class 0 OID 19204)
-- Dependencies: 231
-- Data for Name: rmt_tariff_promotion_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3099 (class 0 OID 19210)
-- Dependencies: 232
-- Data for Name: rmt_tariff_service_type; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3100 (class 0 OID 19216)
-- Dependencies: 233
-- Data for Name: test_menu; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (1, 'VIEW', 'S', 0, 1, 'View Data (for users)', 'USER', NULL, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (3, 'VIEW', 'S', 0, 1, 'View Data (for appl_admin)', 'ADMIN', NULL, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (5, 'VIEW', 'S', 0, 1, 'View Data (for dba_admin)', 'DBADMIN', NULL, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (60, 'Consumption Code', 'I', 1, 6, 'Consumption Code', 'ADMIN', 54, '1', 'consumption_code.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (67, 'Consumption Code', 'I', 1, 6, 'Consumption Code', 'USER', 61, '1', 'consumption_code.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (23, 'Create an user', 'I', 1, 2, 'Create an user for the application (for dba admin)', 'DBADMIN', 6, '1', 'create_user.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (25, 'PROMOTIONS', 'S', 1, 1, 'Promotions', 'ADMIN', 24, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (18, 'Application admin', 'I', 2, 1, 'List of all application admin (for dba admin)', 'DBADMIN', 17, '1', 'list_of_app_admin.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (12, 'Application admin', 'I', 2, 1, 'List of all application admin (for appl_admin)', 'ADMIN', 11, '1', 'list_of_app_admin.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (19, 'DBA admin', 'I', 2, 2, 'List of all dba admin (for dba admin)', 'DBADMIN', 17, '1', 'list_of_dba_admin.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (2, 'MODIFY', 'S', 0, 2, 'Modify Data (for users)', 'USER', NULL, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (4, 'MODIFY', 'S', 0, 2, 'Modify Data (for appl_admin)', 'ADMIN', NULL, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (6, 'MODIFY', 'S', 0, 2, 'Modify Data (for dba_admin)', 'DBADMIN', NULL, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (15, 'Other password', 'I', 2, 2, 'Change the password for other user than actual user (for appl_admin)', 'ADMIN', 13, '1', 'change_other_password.xhtlm');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (22, 'Other password', 'I', 2, 2, 'Change the password for other user than actual user (for dba admin)', 'DBADMIN', 20, '1', 'change_other_password.xhtlm');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (7, 'List of users', 'I', 1, 1, 'All user list in the application (for users)', 'USER', 1, '1', 'list_of_users.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (13, 'Change password', 'S', 1, 1, 'Change the password for an user (for appl_admin)', 'ADMIN', 4, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (11, 'List of admin', 'S', 1, 2, 'All admin list in the application (for appl_admin)', 'ADMIN', 3, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (17, 'List of admin', 'S', 1, 2, 'All admin list in the application (for dba_admin)', 'DBADMIN', 5, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (24, 'PARAMETRICS', 'S', 0, 3, 'Parametrics data', 'ADMIN', NULL, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (20, 'Change password', 'S', 1, 1, '6Change the password for an user (for dba admin)', 'DBADMIN', 6, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (8, 'Change password', 'S', 1, 1, 'Change the password for an user (for users)', 'USER', 2, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (26, 'Application Level', 'I', 2, 1, 'Application level to the promotion', 'ADMIN', 25, '1', 'application_level.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (9, 'My password', 'I', 2, 1, 'Change the password for the actual user (users)', 'USER', 8, '1', 'change_my_password.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (14, 'My password', 'I', 2, 1, 'Change the password for the actual user (for appl_admin)', 'ADMIN', 13, '1', 'change_my_password.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (21, 'My password', 'I', 2, 1, 'Change the password for the actual user (for dba admin)', 'DBADMIN', 20, '1', 'change_my_password.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (10, 'List of users', 'I', 1, 1, 'All user list in the application (for appl_admin)', 'ADMIN', 3, '1', 'list_of_users.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (16, 'List of users', 'I', 1, 1, 'All user list in the application (for dba_admin)', 'DBADMIN', 5, '1', 'list_of_users.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (27, 'Application Unit', 'I', 2, 2, 'Application unit to the promotion', 'ADMIN', 25, '1', 'application_unit.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (61, 'CATALOG', 'S', 0, 5, 'Catalog', 'USER', NULL, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (62, 'Product Type', 'I', 1, 1, 'Product Type', 'USER', 61, '1', 'product_type.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (66, 'Promotion Plan Type', 'I', 1, 5, 'Promotion Plan Type', 'USER', 61, '1', 'promotion_plan_type.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (65, 'Promotion Type', 'I', 1, 4, 'Promotion Type', 'USER', 61, '1', 'promotion_type.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (64, 'Equipment Type', 'I', 1, 3, 'Equipment Type', 'USER', 61, '1', 'equipment_type.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (63, 'Service Type', 'I', 1, 2, 'Service Type', 'USER', 61, '1', 'service_type.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (31, 'Consumption Types', 'I', 2, 1, 'Consumption types', 'ADMIN', 30, '1', 'consumption_type.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (29, 'Discount Concepts', 'I', 2, 4, 'Discount concept for the promotion', 'ADMIN', 25, '1', 'discount_concept.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (30, 'CONSUMPTIONS', 'S', 1, 1, 'Consumptions', 'ADMIN', 24, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (32, 'OTHERS', 'S', 1, 1, 'Others', 'ADMIN', 24, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (33, 'Business Scope', 'I', 2, 1, 'Bussines scope', 'ADMIN', 32, '1', 'business_scope.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (34, 'Recurrence', 'I', 2, 2, 'Recurrence of the charges', 'ADMIN', 32, '1', 'recurrence.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (36, 'PARAMETRICS', 'S', 0, 3, 'Parametrics data', 'USER', NULL, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (48, 'ENTITIES', 'S', 0, 4, 'Entities', 'ADMIN', NULL, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (49, 'Entity Types', 'I', 1, 1, 'Entity types', 'ADMIN', 48, '1', 'entity_type.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (50, 'Status', 'I', 1, 2, 'Status of the entities', 'ADMIN', 48, '1', 'status.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (51, 'ENTITIES', 'S', 0, 4, 'Entities', 'USER', NULL, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (52, 'Entity Types', 'I', 1, 1, 'Entity types', 'USER', 51, '1', 'entity_type.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (53, 'Status', 'I', 1, 2, 'Status of the entities', 'USER', 51, '1', 'status.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (54, 'CATALOG', 'S', 0, 5, 'Catalog', 'ADMIN', NULL, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (55, 'Product Type', 'I', 1, 1, 'Product Type', 'ADMIN', 54, '1', 'product_type.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (56, 'Service Type', 'I', 1, 2, 'Service Type', 'ADMIN', 54, '1', 'service_type.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (57, 'Equipment Type', 'I', 1, 3, 'Equipment Type', 'ADMIN', 54, '1', 'equipment_type.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (58, 'Promotion Type', 'I', 1, 4, 'Promotion Type', 'ADMIN', 54, '1', 'promotion_type.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (59, 'Promotion Plan Type', 'I', 1, 5, 'Promotion Plan Type', 'ADMIN', 54, '1', 'promotion_plan_type.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (37, 'PROMOTIONS', 'S', 1, 1, 'Promotions', 'USER', 36, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (38, 'Application Levels', 'I', 2, 1, 'Application level to the promotion', 'USER', 37, '1', 'application_level.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (39, 'Application Units', 'I', 2, 2, 'Application unit to the promotion', 'USER', 37, '1', 'application_unit.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (41, 'Discount Concepts', 'I', 2, 4, 'Discount concept for the promotion', 'USER', 37, '1', 'discount_concept.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (43, 'Consumption Types', 'I', 2, 1, 'Consumption types', 'USER', 42, '1', 'consumption_type.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (35, 'Technology Scope', 'I', 2, 3, 'Technology scope of the entities', 'ADMIN', 32, '1', 'technology_scope.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (47, 'Technology Scope', 'I', 2, 3, 'Technology scope of the entities', 'USER', 44, '1', 'technology_scope.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (42, 'CONSUMPTIONS', 'S', 1, 1, 'Consumptions', 'USER', 36, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (45, 'Business Scope', 'I', 2, 1, 'Bussines scope', 'USER', 44, '1', 'business_scope.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (46, 'Recurrence', 'I', 2, 2, 'Recurrence of the charges', 'USER', 44, '1', 'recurrence.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (44, 'OTHERS', 'S', 1, 1, 'Others', 'USER', 36, '1', NULL);
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (28, 'Discount Types', 'I', 2, 3, 'Discount type for the promotion', 'ADMIN', 25, '1', 'discount_type.xhtml');
INSERT INTO test_menu (menu_id, menu_code, menu_type, menu_level, "position", description, profile_code, submenu_id, status, page) VALUES (40, 'Discount Types', 'I', 2, 3, 'Discount type for the promotion', 'USER', 37, '1', 'discount_type.xhtml');


--
-- TOC entry 3101 (class 0 OID 19219)
-- Dependencies: 234
-- Data for Name: test_user; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO test_user (user_id, user_code, profile_id, profile_code, password) VALUES (1, 'billing_admin', 1, 'DBA_ADMIN', 'b412519dfd76f8ccd682488239e31577');
INSERT INTO test_user (user_id, user_code, profile_id, profile_code, password) VALUES (4, 'user', 3, 'USER', 'ee11cbb19052e40b07aac0ca060c23ee');
INSERT INTO test_user (user_id, user_code, profile_id, profile_code, password) VALUES (2, 'billing_appl', 2, 'APPL_ADMIN', '15b401794cf74c38e1b7c89508eec939');
INSERT INTO test_user (user_id, user_code, profile_id, profile_code, password) VALUES (3, 'billing_appl_2', 2, 'APPL_ADMIN', '15b401794cf74c38e1b7c89508eec939');


--
-- TOC entry 3102 (class 0 OID 19222)
-- Dependencies: 235
-- Data for Name: tid_consumption_code_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3739 (class 0 OID 0)
-- Dependencies: 236
-- Name: tid_consumption_code_id_consumption_code_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_consumption_code_id_consumption_code_id_seq', 1, false);


--
-- TOC entry 3104 (class 0 OID 19227)
-- Dependencies: 237
-- Data for Name: tid_equipment_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3740 (class 0 OID 0)
-- Dependencies: 238
-- Name: tid_equipment_type_id_equipment_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_equipment_type_id_equipment_type_id_seq', 1, false);


--
-- TOC entry 3106 (class 0 OID 19232)
-- Dependencies: 239
-- Data for Name: tid_fee_code_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3741 (class 0 OID 0)
-- Dependencies: 240
-- Name: tid_fee_code_id_fee_code_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_fee_code_id_fee_code_id_seq', 1, false);


--
-- TOC entry 3108 (class 0 OID 19237)
-- Dependencies: 241
-- Data for Name: tid_fee_equipment_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3742 (class 0 OID 0)
-- Dependencies: 242
-- Name: tid_fee_equipment_type_id_fee_equipment_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_fee_equipment_type_id_fee_equipment_type_id_seq', 1, false);


--
-- TOC entry 3110 (class 0 OID 19242)
-- Dependencies: 243
-- Data for Name: tid_fee_product_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3743 (class 0 OID 0)
-- Dependencies: 244
-- Name: tid_fee_product_type_id_fee_product_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_fee_product_type_id_fee_product_type_id_seq', 1, false);


--
-- TOC entry 3112 (class 0 OID 19247)
-- Dependencies: 245
-- Data for Name: tid_fee_promotion_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3744 (class 0 OID 0)
-- Dependencies: 246
-- Name: tid_fee_promotion_type_id_fee_promotion_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_fee_promotion_type_id_fee_promotion_type_id_seq', 1, false);


--
-- TOC entry 3114 (class 0 OID 19252)
-- Dependencies: 247
-- Data for Name: tid_fee_service_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3745 (class 0 OID 0)
-- Dependencies: 248
-- Name: tid_fee_service_type_id_fee_service_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_fee_service_type_id_fee_service_type_id_seq', 1, false);


--
-- TOC entry 3116 (class 0 OID 19257)
-- Dependencies: 249
-- Data for Name: tid_plan_charge_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3746 (class 0 OID 0)
-- Dependencies: 250
-- Name: tid_plan_charge_type_id_plan_charge_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_plan_charge_type_id_plan_charge_type_id_seq', 1, false);


--
-- TOC entry 3118 (class 0 OID 19262)
-- Dependencies: 251
-- Data for Name: tid_plan_discount_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3747 (class 0 OID 0)
-- Dependencies: 252
-- Name: tid_plan_discount_type_id_plan_discount_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_plan_discount_type_id_plan_discount_type_id_seq', 1, false);


--
-- TOC entry 3120 (class 0 OID 19267)
-- Dependencies: 253
-- Data for Name: tid_plan_prerreq_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3748 (class 0 OID 0)
-- Dependencies: 254
-- Name: tid_plan_prerreq_type_id_plan_prerreq_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_plan_prerreq_type_id_plan_prerreq_type_id_seq', 1, false);


--
-- TOC entry 3122 (class 0 OID 19272)
-- Dependencies: 255
-- Data for Name: tid_plan_promotion_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3749 (class 0 OID 0)
-- Dependencies: 256
-- Name: tid_plan_promotion_type_id_plan_promotion_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_plan_promotion_type_id_plan_promotion_type_id_seq', 1, false);


--
-- TOC entry 3124 (class 0 OID 19277)
-- Dependencies: 257
-- Data for Name: tid_plan_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3750 (class 0 OID 0)
-- Dependencies: 258
-- Name: tid_plan_type_id_plan_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_plan_type_id_plan_type_id_seq', 1, false);


--
-- TOC entry 3126 (class 0 OID 19282)
-- Dependencies: 259
-- Data for Name: tid_product_service_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3751 (class 0 OID 0)
-- Dependencies: 260
-- Name: tid_product_service_type_id_product_service_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_product_service_type_id_product_service_type_id_seq', 1, false);


--
-- TOC entry 3128 (class 0 OID 19287)
-- Dependencies: 261
-- Data for Name: tid_product_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO tid_product_type_id (product_type_id) VALUES (1);
INSERT INTO tid_product_type_id (product_type_id) VALUES (2);
INSERT INTO tid_product_type_id (product_type_id) VALUES (3);
INSERT INTO tid_product_type_id (product_type_id) VALUES (4);


--
-- TOC entry 3752 (class 0 OID 0)
-- Dependencies: 262
-- Name: tid_product_type_id_product_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_product_type_id_product_type_id_seq', 4, true);


--
-- TOC entry 3130 (class 0 OID 19292)
-- Dependencies: 263
-- Data for Name: tid_profile_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO tid_profile_id (profile_id) VALUES (1);
INSERT INTO tid_profile_id (profile_id) VALUES (2);
INSERT INTO tid_profile_id (profile_id) VALUES (3);


--
-- TOC entry 3753 (class 0 OID 0)
-- Dependencies: 264
-- Name: tid_profile_id_profile_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_profile_id_profile_id_seq', 1, false);


--
-- TOC entry 3132 (class 0 OID 19297)
-- Dependencies: 265
-- Data for Name: tid_promotion_equipment_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3754 (class 0 OID 0)
-- Dependencies: 266
-- Name: tid_promotion_equipment_type_id_prom_equip_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_promotion_equipment_type_id_prom_equip_type_id_seq', 1, false);


--
-- TOC entry 3134 (class 0 OID 19302)
-- Dependencies: 267
-- Data for Name: tid_promotion_product_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3755 (class 0 OID 0)
-- Dependencies: 268
-- Name: tid_promotion_product_type_id_prom_prod_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_promotion_product_type_id_prom_prod_type_id_seq', 1, false);


--
-- TOC entry 3136 (class 0 OID 19307)
-- Dependencies: 269
-- Data for Name: tid_promotion_promotion_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3756 (class 0 OID 0)
-- Dependencies: 270
-- Name: tid_promotion_promotion_type_id_prom_prom_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_promotion_promotion_type_id_prom_prom_type_id_seq', 1, false);


--
-- TOC entry 3138 (class 0 OID 19312)
-- Dependencies: 271
-- Data for Name: tid_promotion_service_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3757 (class 0 OID 0)
-- Dependencies: 272
-- Name: tid_promotion_service_type_id_prom_serv_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_promotion_service_type_id_prom_serv_type_id_seq', 1, false);


--
-- TOC entry 3140 (class 0 OID 19317)
-- Dependencies: 273
-- Data for Name: tid_promotion_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3758 (class 0 OID 0)
-- Dependencies: 274
-- Name: tid_promotion_type_id_promotion_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_promotion_type_id_promotion_type_id_seq', 1, false);


--
-- TOC entry 3142 (class 0 OID 19322)
-- Dependencies: 275
-- Data for Name: tid_service_equipment_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3759 (class 0 OID 0)
-- Dependencies: 276
-- Name: tid_service_equipment_type_id_service_equipment_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_service_equipment_type_id_service_equipment_type_id_seq', 1, false);


--
-- TOC entry 3144 (class 0 OID 19327)
-- Dependencies: 277
-- Data for Name: tid_service_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO tid_service_type_id (service_type_id) VALUES (1);
INSERT INTO tid_service_type_id (service_type_id) VALUES (2);
INSERT INTO tid_service_type_id (service_type_id) VALUES (3);


--
-- TOC entry 3760 (class 0 OID 0)
-- Dependencies: 278
-- Name: tid_service_type_id_service_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_service_type_id_service_type_id_seq', 3, true);


--
-- TOC entry 3146 (class 0 OID 19332)
-- Dependencies: 279
-- Data for Name: tid_tariff_equipment_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3761 (class 0 OID 0)
-- Dependencies: 280
-- Name: tid_tariff_equipment_type_id_tariff_equipment_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_tariff_equipment_type_id_tariff_equipment_type_id_seq', 1, false);


--
-- TOC entry 3148 (class 0 OID 19337)
-- Dependencies: 281
-- Data for Name: tid_tariff_product_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3762 (class 0 OID 0)
-- Dependencies: 282
-- Name: tid_tariff_product_type_id_tariff_product_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_tariff_product_type_id_tariff_product_type_id_seq', 1, false);


--
-- TOC entry 3150 (class 0 OID 19342)
-- Dependencies: 283
-- Data for Name: tid_tariff_promotion_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3763 (class 0 OID 0)
-- Dependencies: 284
-- Name: tid_tariff_promotion_type_id_tariff_promotion_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_tariff_promotion_type_id_tariff_promotion_type_id_seq', 1, false);


--
-- TOC entry 3152 (class 0 OID 19347)
-- Dependencies: 285
-- Data for Name: tid_tariff_service_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3764 (class 0 OID 0)
-- Dependencies: 286
-- Name: tid_tariff_service_type_id_tariff_service_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_tariff_service_type_id_tariff_service_type_id_seq', 1, false);


--
-- TOC entry 3154 (class 0 OID 19352)
-- Dependencies: 287
-- Data for Name: tid_tariff_type_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--



--
-- TOC entry 3765 (class 0 OID 0)
-- Dependencies: 288
-- Name: tid_tariff_type_id_tariff_type_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_tariff_type_id_tariff_type_id_seq', 1, false);


--
-- TOC entry 3156 (class 0 OID 19357)
-- Dependencies: 289
-- Data for Name: tid_user_id; Type: TABLE DATA; Schema: billing; Owner: billing_admin
--

INSERT INTO tid_user_id (user_id) VALUES (1);
INSERT INTO tid_user_id (user_id) VALUES (2);
INSERT INTO tid_user_id (user_id) VALUES (3);
INSERT INTO tid_user_id (user_id) VALUES (4);
INSERT INTO tid_user_id (user_id) VALUES (5);
INSERT INTO tid_user_id (user_id) VALUES (9);
INSERT INTO tid_user_id (user_id) VALUES (13);
INSERT INTO tid_user_id (user_id) VALUES (7);
INSERT INTO tid_user_id (user_id) VALUES (112);
INSERT INTO tid_user_id (user_id) VALUES (114);
INSERT INTO tid_user_id (user_id) VALUES (115);
INSERT INTO tid_user_id (user_id) VALUES (116);


--
-- TOC entry 3766 (class 0 OID 0)
-- Dependencies: 290
-- Name: tid_user_id_user_id_seq; Type: SEQUENCE SET; Schema: billing; Owner: billing_admin
--

SELECT pg_catalog.setval('tid_user_id_user_id_seq', 116, true);


-- Completed on 2017-11-01 15:01:05 CET

--
-- PostgreSQL database dump complete
--

