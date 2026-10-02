-- Sample catalogue. Rows carry explicit ids and use INSERT IGNORE, so running
-- this script again on start-up never duplicates anything.

INSERT IGNORE INTO providers (provider_id, company_name, phone, email, city) VALUES
 (1, 'Yangtze River Books',   '025-8431-1001', 'orders@yangtzebooks.example', 'Nanjing'),
 (2, 'Pearl Academic Press',  '020-3357-2200', 'sales@pearlpress.example',    'Guangzhou'),
 (3, 'Northern Lights Media', '010-6629-4410', 'trade@northlights.example',   'Beijing');

INSERT IGNORE INTO authors (author_id, full_name, nationality, email, biography) VALUES
 (1, 'Grace Hopkins',  'United States', 'grace.h@authors.example', 'Compiler engineer turned teacher; writes approachable programming books.'),
 (2, 'Wei Jianguo',    'China',         'wei.jg@authors.example',  'Professor of software engineering with 20 years of industry consulting.'),
 (3, 'Amara Okafor',   'Nigeria',       'amara.o@authors.example', 'Data scientist focused on practical statistics and visualisation.'),
 (4, 'Lars Eriksson',  'Sweden',        'lars.e@authors.example',  'Distributed-systems architect and conference speaker.'),
 (5, 'Sun Meiling',    'China',         'sun.ml@authors.example',  'Novelist whose stories explore life in modern Chinese cities.'),
 (6, 'Priya Raman',    'India',         'priya.r@authors.example', 'UX researcher who writes about human-centred design.');

INSERT IGNORE INTO books (book_id, title, isbn, category, price, stock, publish_year, summary, provider_id) VALUES
 (1,  'Compilers Without Fear',              '9787300000011', 'Computer Science', 68.00, 24, 2021, 'A gentle, project-based tour through lexing, parsing and code generation.', 1),
 (2,  'Software Architecture in Practice',   '9787300000028', 'Software Engineering', 89.50, 12, 2022, 'Patterns, trade-offs and case studies for designing large systems.', 2),
 (3,  'Statistics for the Curious',          '9787300000035', 'Data Science', 45.00, 3, 2020, 'Everyday statistics explained with real data sets and plenty of plots.', 3),
 (4,  'Designing Data-Heavy Services',       '9787300000042', 'Computer Science', 99.00, 7, 2023, 'Replication, partitioning and consistency for modern back ends.', 2),
 (5,  'The Lantern Street Tea House',        '9787300000059', 'Fiction', 39.80, 40, 2019, 'Three generations of a family and the tea house that holds them together.', 1),
 (6,  'Requirements That Work',              '9787300000066', 'Software Engineering', 52.00, 0, 2018, 'Writing, validating and managing software requirements that teams actually use.', 2),
 (7,  'Visual Thinking with Data',           '9787300000073', 'Data Science', 58.60, 15, 2022, 'How to turn numbers into charts people understand.', 3),
 (8,  'People First: Human-Centred Design',  '9787300000080', 'Design', 47.30, 9, 2021, 'Research methods and design practice for usable products.', 3),
 (9,  'Night Train to Chongqing',            '9787300000097', 'Fiction', 35.00, 2, 2023, 'A mystery that unfolds over a single overnight train journey.', 1),
 (10, 'Testing Distributed Systems',         '9787300000103', 'Software Engineering', 76.00, 11, 2024, 'Fault injection, chaos experiments and verification for services at scale.', NULL),
 (11, 'Introduction to Algorithms Visualised','9787300000110', 'Computer Science', 62.40, 18, 2020, 'Classic algorithms explained step by step with diagrams.', 1),
 (12, 'Design Systems Handbook',             '9787300000127', 'Design', 55.00, 4, 2024, 'Building and maintaining component libraries and design tokens.', 3);

INSERT IGNORE INTO book_authors (book_id, author_id, author_order) VALUES
 (1, 1, 1),
 (2, 2, 1), (2, 4, 2),
 (3, 3, 1),
 (4, 4, 1),
 (5, 5, 1),
 (6, 2, 1),
 (7, 3, 1), (7, 6, 2),
 (8, 6, 1),
 (9, 5, 1),
 (10, 4, 1), (10, 1, 2),
 (11, 1, 1), (11, 2, 2),
 (12, 6, 1);
