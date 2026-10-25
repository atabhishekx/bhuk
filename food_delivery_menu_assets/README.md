# Food Delivery Menu Asset Pack

This pack contains a demo menu for a single Indian restaurant chain, with 50 menu entries and one individual JPG image file per item.

## Contents

- `images/` — 50 individual 512×512 JPEG food image crops.
- `data/menu.json` — structured menu data for application seeding.
- `data/menu.csv` — spreadsheet-friendly copy of the menu dataset.
- `README.md` — integration notes.

## Menu fields

Each menu record includes:
- `id`: stable demo ID such as `FD001`
- `name`
- `description`
- `category`
- `price_inr`
- `dietary_type`: `veg` or `non_veg`
- `is_vegetarian`
- `image`: relative image path
- `available`
- `currency`: `INR`

## Example JSON record

```json
{
  "id": "FD001",
  "name": "Butter Chicken",
  "description": "Tender chicken in a creamy tomato-butter gravy, finished with aromatic spices.",
  "category": "North Indian",
  "price_inr": 320,
  "dietary_type": "non_veg",
  "is_vegetarian": false,
  "image": "images/001_butter_chicken.jpg",
  "available": true,
  "currency": "INR"
}
```

## Using the assets

1. Extract the ZIP.
2. Copy `images/` into your application's static/public assets directory.
3. Import `data/menu.json` into your development seed script or database.
4. Resolve each `image` path relative to the static assets root.
5. In production, store images in object storage/CDN and persist the resulting URL in the menu record.

## Important asset note

The images are generated AI menu imagery, extracted into individual image files from a single generated menu grid. They are useful for prototypes, demos, and development seed data. Because the source was a grid, crops may retain slight visual inconsistencies and are not guaranteed to be perfect standalone commercial food photographs. Review them before production use.

## Pricing and food information

Prices and descriptions are illustrative demo data, not a real restaurant's verified menu. Confirm actual prices, portion sizes, allergens, dietary claims, and availability with the restaurant before launch. `veg`/`non_veg` labels are demo classifications; validate them before displaying to customers.

## File naming

Image names begin with the item number and use lowercase snake_case. Dataset image paths map directly to these files.
