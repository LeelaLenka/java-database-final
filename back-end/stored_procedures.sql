USE inventory;

-- Drop old versions so this script can be re-run safely
DROP PROCEDURE IF EXISTS GetMonthlySalesForEachStore;
DROP PROCEDURE IF EXISTS GetAggregateSalesForCompany;
DROP PROCEDURE IF EXISTS GetTopSellingProductsByCategory;
DROP PROCEDURE IF EXISTS GetTopSellingProductByStore;

DELIMITER $$

-- 1. Monthly sales for each store
-- Example: CALL GetMonthlySalesForEachStore(2025, 3);
CREATE PROCEDURE GetMonthlySalesForEachStore(IN year_param INT, IN month_param INT)
BEGIN
    SELECT
        od.store_id,
        SUM(DISTINCT od.total_price) AS total_sales,
        MONTH(od.date) AS sale_month,
        YEAR(od.date)  AS sale_year
    FROM order_details od
    WHERE YEAR(od.date) = year_param
      AND MONTH(od.date) = month_param
    GROUP BY od.store_id, MONTH(od.date), YEAR(od.date)
    ORDER BY total_sales DESC;
END $$

-- 2. Aggregate sales for the whole company
-- Example: CALL GetAggregateSalesForCompany(2025, 3);
CREATE PROCEDURE GetAggregateSalesForCompany(IN year_param INT, IN month_param INT)
BEGIN
    SELECT
        SUM(DISTINCT od.total_price) AS total_sales,
        MONTH(od.date) AS sale_month,
        YEAR(od.date)  AS sale_year
    FROM order_details od
    WHERE YEAR(od.date) = year_param
      AND MONTH(od.date) = month_param
    GROUP BY MONTH(od.date), YEAR(od.date);
END $$

-- 3. Top-selling product(s) in each category (ties are all shown)
-- Example: CALL GetTopSellingProductsByCategory(3, 2025);
CREATE PROCEDURE GetTopSellingProductsByCategory(IN target_month INT, IN target_year INT)
BEGIN
    SELECT
        p.category,
        p.name,
        SUM(oi.quantity) AS total_quantity_sold,
        SUM(oi.quantity * oi.price) AS total_sales
    FROM order_item oi
    JOIN order_details od ON oi.order_id = od.id
    JOIN product p        ON oi.product_id = p.id
    WHERE MONTH(od.date) = target_month
      AND YEAR(od.date)  = target_year
    GROUP BY p.category, p.name
    HAVING SUM(oi.quantity) = (
        SELECT MAX(sq.qty)
        FROM (
            SELECT p2.category, SUM(oi2.quantity) AS qty
            FROM order_item oi2
            JOIN order_details od2 ON oi2.order_id = od2.id
            JOIN product p2        ON oi2.product_id = p2.id
            WHERE MONTH(od2.date) = target_month
              AND YEAR(od2.date)  = target_year
            GROUP BY p2.category, p2.name
        ) sq
        WHERE sq.category = p.category
    )
    ORDER BY p.category, p.name;
END $$

-- 4. Top-selling product(s) in each store (ties are all shown)
-- Example: CALL GetTopSellingProductByStore(3, 2025);
CREATE PROCEDURE GetTopSellingProductByStore(IN target_month INT, IN target_year INT)
BEGIN
    SELECT
        p.name AS product_name,
        od.store_id,
        SUM(oi.quantity) AS total_quantity_sold,
        SUM(oi.quantity * oi.price) AS total_sales
    FROM order_item oi
    JOIN order_details od ON oi.order_id = od.id
    JOIN product p        ON oi.product_id = p.id
    WHERE MONTH(od.date) = target_month
      AND YEAR(od.date)  = target_year
    GROUP BY od.store_id, p.name
    HAVING SUM(oi.quantity) = (
        SELECT MAX(sq.qty)
        FROM (
            SELECT od2.store_id, SUM(oi2.quantity) AS qty
            FROM order_item oi2
            JOIN order_details od2 ON oi2.order_id = od2.id
            JOIN product p2        ON oi2.product_id = p2.id
            WHERE MONTH(od2.date) = target_month
              AND YEAR(od2.date)  = target_year
            GROUP BY od2.store_id, p2.name
        ) sq
        WHERE sq.store_id = od.store_id
    )
    ORDER BY od.store_id, p.name;
END $$

DELIMITER ;