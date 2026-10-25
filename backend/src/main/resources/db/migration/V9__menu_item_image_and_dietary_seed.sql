-- =============================================================
-- V9__menu_item_image_and_dietary_seed.sql
-- Restru — Add image_url + dietary_type to menu_items and seed
-- the food_delivery_menu_assets (50 items, Indian cuisine).
-- =============================================================

-- ----- New columns on menu_items -----
ALTER TABLE menu_items
    ADD COLUMN image_url VARCHAR(500) NULL,
    ADD COLUMN dietary_type VARCHAR(20) NULL;

-- ----- Seed categories from the asset pack (outlet 1) -----
-- These will get auto-increment IDs 1-7
INSERT INTO menu_categories (outlet_id, name, description, display_order, active) VALUES
(1, 'North Indian', 'Curries, tandoori, breads from the north', 1, TRUE),
(1, 'Rice & Biryani', 'Fragrant basmati rice and biryanis', 2, TRUE),
(1, 'South Indian', 'Dosas, idlis, uttapams and rice bowls from the south', 3, TRUE),
(1, 'Snacks', 'Fried and baked savoury bites', 4, TRUE),
(1, 'Sides', 'Salads, raita and light accompaniments', 5, TRUE),
(1, 'Desserts', 'Traditional Indian sweets', 6, TRUE),
(1, 'Beverages', 'Tea, coffee and cold drinks', 7, TRUE);

-- ----- Seed the 50 menu items from food_delivery_menu_assets -----
-- base_price_minor in paise (price_inr * 100)
-- image_url served from frontend/public/menu-images/
-- category_id mapping: 1=North Indian, 2=Rice & Biryani, 3=South Indian, 4=Snacks, 5=Sides, 6=Desserts, 7=Beverages
INSERT INTO menu_items (category_id, name, description, base_price_minor, active, available, image_url, dietary_type) VALUES
(1, 'Butter Chicken', 'Tender chicken in a creamy tomato-butter gravy, finished with aromatic spices.', 32000, TRUE, TRUE, '/menu-images/001_butter_chicken.jpg', 'non_veg'),
(1, 'Chicken Tikka Masala', 'Char-grilled chicken tikka in a rich, spiced tomato masala.', 31000, TRUE, TRUE, '/menu-images/002_chicken_tikka_masala.jpg', 'non_veg'),
(1, 'Mutton Rogan Josh', 'Slow-cooked mutton in a fragrant Kashmiri-style gravy.', 36000, TRUE, TRUE, '/menu-images/003_mutton_rogan_josh.jpg', 'non_veg'),
(1, 'Paneer Butter Masala', 'Soft paneer cubes in a smooth tomato, butter, and cream gravy.', 28000, TRUE, TRUE, '/menu-images/004_paneer_butter_masala.jpg', 'veg'),
(1, 'Kadai Paneer', 'Paneer tossed with capsicum, onion, and freshly ground kadai spices.', 27000, TRUE, TRUE, '/menu-images/005_kadai_paneer.jpg', 'veg'),
(1, 'Palak Paneer', 'Paneer served in a seasoned spinach gravy.', 26000, TRUE, TRUE, '/menu-images/006_palak_paneer.jpg', 'veg'),
(1, 'Chole Bhature', 'Spiced chickpea curry served with fluffy fried bhature.', 18000, TRUE, TRUE, '/menu-images/007_chole_bhature.jpg', 'veg'),
(1, 'Dal Makhani', 'Slow-simmered black lentils finished with butter and cream.', 24000, TRUE, TRUE, '/menu-images/008_dal_makhani.jpg', 'veg'),
(1, 'Rajma Chawal', 'Punjabi-style kidney bean curry served with steamed rice.', 20000, TRUE, TRUE, '/menu-images/009_rajma_chawal.jpg', 'veg'),
(1, 'Aloo Paratha', 'Whole-wheat flatbread stuffed with seasoned mashed potatoes.', 12000, TRUE, TRUE, '/menu-images/010_aloo_paratha.jpg', 'veg'),
(1, 'Stuffed Naan', 'Soft tandoor-baked naan with a savoury stuffing.', 8000, TRUE, TRUE, '/menu-images/011_stuffed_naan.jpg', 'veg'),
(1, 'Garlic Naan', 'Tandoor-baked naan brushed with garlic and herbs.', 7000, TRUE, TRUE, '/menu-images/012_garlic_naan.jpg', 'veg'),
(1, 'Tandoori Roti', 'Whole-wheat flatbread baked in a traditional tandoor.', 4000, TRUE, TRUE, '/menu-images/013_tandoori_roti.jpg', 'veg'),
(1, 'Lachha Paratha', 'Flaky, layered Indian flatbread.', 7000, TRUE, TRUE, '/menu-images/014_lachha_paratha.jpg', 'veg'),

(2, 'Steamed Rice', 'Fluffy steamed rice, a simple accompaniment to curries.', 6000, TRUE, TRUE, '/menu-images/015_steamed_rice.jpg', 'veg'),
(2, 'Jeera Rice', 'Basmati rice tempered with cumin and whole spices.', 8000, TRUE, TRUE, '/menu-images/016_jeera_rice.jpg', 'veg'),
(2, 'Veg Biryani', 'Fragrant basmati rice layered with vegetables and biryani spices.', 22000, TRUE, TRUE, '/menu-images/017_veg_biryani.jpg', 'veg'),
(2, 'Chicken Biryani', 'Aromatic basmati rice layered with spiced chicken.', 30000, TRUE, TRUE, '/menu-images/018_chicken_biryani.jpg', 'non_veg'),
(2, 'Mutton Biryani', 'Slow-cooked mutton and fragrant rice with traditional biryani spices.', 34000, TRUE, TRUE, '/menu-images/019_mutton_biryani.jpg', 'non_veg'),
(2, 'Hyderabadi Biryani', 'Hyderabadi-style dum biryani with aromatic spices.', 33000, TRUE, TRUE, '/menu-images/020_hyderabadi_biryani.jpg', 'non_veg'),

(3, 'Masala Dosa', 'Crisp dosa filled with spiced potato masala, served with chutneys.', 14000, TRUE, TRUE, '/menu-images/021_masala_dosa.jpg', 'veg'),
(3, 'Plain Dosa', 'Thin, crisp fermented-rice-and-lentil crepe with chutneys.', 12000, TRUE, TRUE, '/menu-images/022_plain_dosa.jpg', 'veg'),
(3, 'Rava Dosa', 'Crisp semolina dosa with onion, herbs, and spices.', 13000, TRUE, TRUE, '/menu-images/023_rava_dosa.jpg', 'veg'),
(3, 'Idli Sambar', 'Steamed rice cakes served with sambar and coconut chutney.', 10000, TRUE, TRUE, '/menu-images/024_idli_sambar.jpg', 'veg'),
(3, 'Medu Vada', 'Crisp lentil doughnuts served with sambar and chutney.', 10000, TRUE, TRUE, '/menu-images/025_medu_vada.jpg', 'veg'),
(3, 'Uttapam', 'Soft, savoury rice-lentil pancake topped with vegetables.', 13000, TRUE, TRUE, '/menu-images/026_uttapam.jpg', 'veg'),
(3, 'Pongal', 'Comforting rice and lentil dish tempered with pepper and cumin.', 11000, TRUE, TRUE, '/menu-images/027_pongal.jpg', 'veg'),
(3, 'Poori Masala', 'Puffed pooris served with mildly spiced potato curry.', 12000, TRUE, TRUE, '/menu-images/028_poori_masala.jpg', 'veg'),
(3, 'Lemon Rice', 'Rice tempered with lemon, mustard seeds, curry leaves, and peanuts.', 12000, TRUE, TRUE, '/menu-images/029_lemon_rice.jpg', 'veg'),
(3, 'Curd Rice', 'Cooling rice mixed with yogurt and a light tempering.', 10000, TRUE, TRUE, '/menu-images/030_curd_rice.jpg', 'veg'),

(4, 'Samosa', 'Crisp pastry filled with spiced potato and peas.', 4000, TRUE, TRUE, '/menu-images/031_samosa.jpg', 'veg'),
(4, 'Kachori', 'Flaky, deep-fried pastry with a spiced filling.', 4000, TRUE, TRUE, '/menu-images/032_kachori.jpg', 'veg'),
(4, 'Aloo Tikki', 'Golden potato patties seasoned with Indian spices.', 5000, TRUE, TRUE, '/menu-images/033_aloo_tikki.jpg', 'veg'),
(4, 'Paneer Pakora', 'Paneer pieces coated in seasoned gram-flour batter and fried.', 9000, TRUE, TRUE, '/menu-images/034_paneer_pakora.jpg', 'veg'),
(4, 'Onion Pakora', 'Crispy onion fritters with herbs and spices.', 8000, TRUE, TRUE, '/menu-images/035_onion_pakora.jpg', 'veg'),
(4, 'Chicken 65', 'Spicy, crisp fried chicken bites with curry leaves.', 22000, TRUE, TRUE, '/menu-images/036_chicken_65.jpg', 'non_veg'),
(4, 'Fish Fry', 'Fish marinated in spices and pan-fried until crisp.', 26000, TRUE, TRUE, '/menu-images/037_fish_fry.jpg', 'non_veg'),
(4, 'Chicken Tikka (Dry)', 'Yogurt-marinated chicken grilled with tandoori spices.', 24000, TRUE, TRUE, '/menu-images/038_chicken_tikka_dry.jpg', 'non_veg'),
(4, 'Tandoori Chicken', 'Chicken marinated with yogurt and spices, roasted in a tandoor.', 28000, TRUE, TRUE, '/menu-images/039_tandoori_chicken.jpg', 'non_veg'),
(4, 'Chicken Wings', 'Seasoned chicken wings cooked until browned and flavourful.', 22000, TRUE, TRUE, '/menu-images/040_chicken_wings.jpg', 'non_veg'),
(4, 'French Fries', 'Golden, crisp potato fries with a light seasoning.', 10000, TRUE, TRUE, '/menu-images/041_french_fries.jpg', 'veg'),
(4, 'Masala Papad', 'Crisp papad topped with onion, tomato, and fresh herbs.', 6000, TRUE, TRUE, '/menu-images/042_masala_papad.jpg', 'veg'),

(5, 'Green Salad', 'Fresh seasonal vegetables served as a light side.', 8000, TRUE, TRUE, '/menu-images/043_green_salad.jpg', 'veg'),
(5, 'Raita', 'Cooling yogurt side with cucumber and mild spices.', 8000, TRUE, TRUE, '/menu-images/044_raita.jpg', 'veg'),

(6, 'Gulab Jamun', 'Soft milk-solid dumplings soaked in fragrant sugar syrup.', 10000, TRUE, TRUE, '/menu-images/045_gulab_jamun.jpg', 'veg'),
(6, 'Rasmalai', 'Soft chenna patties served in sweetened, flavoured milk.', 12000, TRUE, TRUE, '/menu-images/046_rasmalai.jpg', 'veg'),
(6, 'Kheer', 'Creamy rice pudding gently flavoured with cardamom.', 10000, TRUE, TRUE, '/menu-images/047_kheer.jpg', 'veg'),

(7, 'Filter Coffee', 'South Indian filter coffee served with milk.', 7000, TRUE, TRUE, '/menu-images/048_filter_coffee.jpg', 'veg'),
(7, 'Masala Chai', 'Indian tea brewed with milk and warming spices.', 5000, TRUE, TRUE, '/menu-images/049_masala_chai.jpg', 'veg'),
(7, 'Fresh Lime Soda', 'Refreshing lime drink with soda, served sweet or salted.', 6000, TRUE, TRUE, '/menu-images/050_fresh_lime_soda.jpg', 'veg');
