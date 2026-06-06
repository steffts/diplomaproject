
-- ============================================================
-- seed.sql  --  Volunteer Platform seed data
-- ============================================================
-- Schema source: JPA entities (Hibernate SpringPhysicalNamingStrategy
-- converts camelCase fields to snake_case column names)
--
-- Tables & real columns
-- ---------------------------------------------------------------
-- users             : id, first_name, last_name, email, password,
--                     role, status, average_rating, rating_count,
--                     created_at, updated_at
-- events            : id, title, description, location, event_date,
--                     participant_limit, category, image_url,
--                     owner_id, created_at, updated_at
-- event_participants : event_id, user_id         (junction -- no id)
-- feedbacks         : id, rating, review_text,
--                     created_at, author_id, event_id
-- chat_messages     : id, event_id, author_id, content, sent_at
-- ============================================================

BEGIN;

-- ============================================================
-- CLEANUP
-- Order: leaf tables (FKs point outward) -> root tables
--   chat_messages -> feedbacks -> event_participants -> events -> users
-- RESTART IDENTITY resets all BIGSERIAL sequences to 1 so that
-- the IDs used in FK columns below are predictable.
-- CASCADE drops any residual FK references automatically.
-- ============================================================
TRUNCATE TABLE
    chat_messages,
    feedbacks,
    event_participants,
    events,
    users
RESTART IDENTITY CASCADE;

-- ============================================================
-- 1. USERS
-- ============================================================
-- Passwords hashed with BCrypt cost 10 ($2b$ prefix).
-- Spring Security BCryptPasswordEncoder accepts $2a$/$2b$/$2y$.
-- Plain-text passwords (for reference / login testing):
--   id=1  ana.admin@example.com    -> Admin@2024
--   id=2  maria.pop@example.com    -> Volunteer1!
--   id=3  ion.ionescu@example.com  -> Volunteer2!
--   id=4  elena.d@example.com      -> Volunteer3!
--   id=5  andrei.c@example.com     -> Volunteer4!
--
-- average_rating / rating_count are pre-computed to match the
-- feedbacks inserted in section 4, using the formula from
-- FeedbackService.updateOrganizerRating():
--   newCount = ratingCount + 1
--   newAvg   = round((averageRating * ratingCount + newRating)
--                    / newCount * 10.0) / 10.0
--
--   User 2 (Maria)  owns Event 1 -> 1 feedback *5  -> avg=5.0, count=1
--   User 3 (Ion)    owns Event 2 -> 1 feedback *4  -> avg=4.0, count=1
--   User 4 (Elena)  owns Event 3 -> feedbacks *5 then *4
--                                   round((0+5)/1)=5.0, then
--                                   round((5+4)/2)=4.5 -> avg=4.5, count=2
--   User 1 (Admin), User 5 (Andrei) -> no feedbacks received -> avg=0.0, count=0
-- ============================================================
INSERT INTO users
    (first_name, last_name, email, password,
     role, status, average_rating, rating_count,
     created_at, updated_at)
VALUES
    -- 1 - Admin
    ('Ana',    'Admin',      'ana.admin@example.com',
     '$2b$10$yfY0BLDm0vy7BUMpG83h/e2Y814i2Yj7f/3dfYyP4Rlj/Ms5ZLnjS',
     'ADMIN', 'ACTIVE', 0.0, 0, NOW(), NOW()),

    -- 2 - Volunteer / owner of Event 1
    ('Maria',  'Pop',        'maria.pop@example.com',
     '$2b$10$VsFSQZlz0zu4Mshk/gd/XumCe3L6lhNnqLaLRpyS9Da/1io7WLp2W',
     'USER', 'ACTIVE', 5.0, 1, NOW(), NOW()),

    -- 3 - Volunteer / owner of Event 2
    ('Ion',    'Ionescu',    'ion.ionescu@example.com',
     '$2b$10$Rc2kWXVAK9PAtTPjM3s6WOIViodsQin./yPsvBQjPDdVVQJSGuSCC',
     'USER', 'ACTIVE', 4.0, 1, NOW(), NOW()),

    -- 4 - Volunteer / owner of Event 3
    ('Elena',  'Dumitrescu', 'elena.d@example.com',
     '$2b$10$ycKolR6ZPc7k1URXAV1vG.PXyFfSIygE5/AoQVisNGSgWZqFeuIPK',
     'USER', 'ACTIVE', 4.5, 2, NOW(), NOW()),

    -- 5 - Volunteer / owner of Event 4
    ('Andrei', 'Constantin', 'andrei.c@example.com',
     '$2b$10$kTbSYT/ynzSUZ/xbP9/3AuiBWAFl1jBWkB7M//fEKHvV/9GSEFY2S',
     'USER', 'ACTIVE', 0.0, 0, NOW(), NOW());

-- ============================================================
-- 2. EVENTS
-- ============================================================
-- Events 1-3  -> event_date in the past so that feedback is allowed
--               (FeedbackService requires eventDate < NOW()).
-- Events 4-5  -> upcoming dates.
-- participant_limit: the live DB enforces NOT NULL on this column
--   (overrides the entity's nullable=true). All events have an explicit limit.
-- image_url format (from FileStorageService): /uploads/events/{uuid}.ext
--   Three real image files exist in the uploads directory and are
--   referenced for events 1-3. Events 4-5 have no image (NULL).
-- owner_id references users.id (sequence starts at 1 after TRUNCATE).
-- ============================================================
INSERT INTO events
    (title, description, location, event_date,
     participant_limit, category, image_url,
     owner_id, created_at, updated_at)
VALUES
    -- Event 1 - ECOLOGY / past / owner=Maria(2)
    ('City Park Cleanup',
     'Join us for a community cleanup of Central Park. Gloves and waste bags are provided. Help us keep our city green and beautiful!',
     'Central Park, Cluj-Napoca',
     '2024-11-10 09:00:00',
     20, 'ECOLOGY',
     '/uploads/events/60d454e3-90b3-4976-aad3-9692825f9e8c.png',
     2, NOW(), NOW()),

    -- Event 2 - EDUCATION / past / owner=Ion(3)
    ('Free Math Tutoring for High School Students',
     'Free preparation sessions for high school students ahead of their final exams. Topics covered: algebra, geometry, and calculus basics.',
     'County Library, Brasov',
     '2024-12-05 14:00:00',
     15, 'EDUCATION',
     '/uploads/events/c3b46ac3-bd26-4528-b60c-0acf44a9b1ca.png',
     3, NOW(), NOW()),

    -- Event 3 - SOCIAL / past / owner=Elena(4)
    ('Helping Elderly Neighbors',
     'We assist elderly individuals living alone with groceries, household tasks, and companionship. Every hour of your time makes a real difference.',
     'Social Center No. 3, Bucharest',
     '2025-01-20 10:00:00',
     50, 'SOCIAL',
     '/uploads/events/c9b0eb2d-6b26-47d0-8781-b0b7f9f40e51.png',
     4, NOW(), NOW()),

    -- Event 4 - ANIMALS / upcoming / owner=Andrei(5)
    ('Animal Shelter Adoption Day',
     'Help the local shelter care for animals and take part in an open adoption event. Dogs, cats, and more are looking for loving homes!',
     'Municipal Animal Shelter, Timisoara',
     '2025-08-15 11:00:00',
     30, 'ANIMALS', NULL,
     5, NOW(), NOW()),

    -- Event 5 - HEALTH / upcoming / owner=Admin(1)
    ('Blood Donation Drive',
     'Community blood donation campaign in partnership with the county hospital. One donation can save up to three lives. Be someone''s hero today!',
     'County Hospital, Iasi',
     '2025-09-01 08:00:00',
     100, 'HEALTH', NULL,
     1, NOW(), NOW());

-- ============================================================
-- 3. EVENT_PARTICIPANTS  (junction table - composite PK)
-- ============================================================
-- A user is never inserted as a participant in their own event
-- (the app prevents this at the service layer).
--
-- Event 1 (owner=2 Maria):  Ion(3), Elena(4), Andrei(5) join
-- Event 2 (owner=3 Ion):    Maria(2), Elena(4) join
-- Event 3 (owner=4 Elena):  Maria(2), Ion(3), Andrei(5) join
-- Event 4 (owner=5 Andrei): Maria(2), Ion(3) join
-- Event 5 (owner=1 Admin):  all four volunteers join
-- ============================================================
INSERT INTO event_participants (event_id, user_id) VALUES
    (1, 3), (1, 4), (1, 5),
    (2, 2), (2, 4),
    (3, 2), (3, 3), (3, 5),
    (4, 2), (4, 3),
    (5, 2), (5, 3), (5, 4), (5, 5);

-- ============================================================
-- 4. FEEDBACKS
-- ============================================================
-- Business rules reproduced from FeedbackService:
--   * event_date must be < NOW()          -> only events 1-3 qualify
--   * author must be a participant OR the owner of the event
--   * one feedback per (author_id, event_id) pair -- no duplicates below
--
-- Event 1 (owner=Maria/2): Ion(3) is participant   -> leaves *5
-- Event 2 (owner=Ion/3):   Maria(2) is participant -> leaves *4
-- Event 3 (owner=Elena/4): Maria(2) participant    -> leaves *5
--                          Ion(3) participant       -> leaves *4
--
-- These four rows produce the average_rating / rating_count values
-- already set in the users INSERT above.
-- ============================================================
INSERT INTO feedbacks
    (rating, review_text, author_id, event_id, created_at)
VALUES
    -- Event 1: Ion(3) rates Maria's event -> *5  (Maria: avg=5.0, count=1)
    (5,
     'Extremely well organized event. The team was friendly and we left the park spotless. Highly recommend volunteering here!',
     3, 1, NOW()),

    -- Event 2: Maria(2) rates Ion's event -> *4  (Ion: avg=4.0, count=1)
    (4,
     'Clear and well-structured tutoring sessions. I finally understood concepts I had been struggling with for months. Great initiative!',
     2, 2, NOW()),

    -- Event 3: Maria(2) rates Elena's event -> *5  (Elena after 1st: avg=5.0, count=1)
    (5,
     'A truly meaningful experience. I felt genuinely useful and helped people who really needed support. Will definitely come back!',
     2, 3, NOW()),

    -- Event 3: Ion(3) rates Elena's event -> *4  (Elena after 2nd: avg=4.5, count=2)
    (4,
     'Solid organization and a warm atmosphere. Would have been even better with more volunteers, but the spirit was excellent.',
     3, 3, NOW());

-- ============================================================
-- 5. CHAT_MESSAGES
-- ============================================================
-- sent_at is provided explicitly because @CreationTimestamp only
-- fires through JPA -- raw SQL inserts require a manual timestamp.
-- Authors are owners or confirmed participants of each event.
-- ============================================================
INSERT INTO chat_messages
    (event_id, author_id, content, sent_at)
VALUES
    -- Event 1 - City Park Cleanup (past)
    (1, 2, 'Hi everyone! We will meet at the main entrance at 9:00 AM sharp. I will bring the waste bags.',
     NOW() - INTERVAL '180 days'),
    (1, 3, 'Got it, thanks! I will bring some extra bags and a spare pair of gloves just in case.',
     NOW() - INTERVAL '180 days'),
    (1, 4, 'I am coming from the city centre and have room in my car if anyone needs a ride.',
     NOW() - INTERVAL '180 days'),
    (1, 5, 'Thanks for the offer, Elena! I will meet the group at the entrance.',
     NOW() - INTERVAL '179 days'),

    -- Event 2 - Free Math Tutoring (past)
    (2, 3, 'Room 3 is reserved for Friday''s session. Please bring notebooks and geometry tools.',
     NOW() - INTERVAL '155 days'),
    (2, 2, 'Thank you, Ion! I will arrive 15 minutes early to help set up the room.',
     NOW() - INTERVAL '155 days'),
    (2, 4, 'I can bring a portable whiteboard if you need extra writing space.',
     NOW() - INTERVAL '154 days'),

    -- Event 4 - Animal Shelter Adoption Day (upcoming)
    (4, 5, 'Hello everyone! Please wear clothes you do not mind getting a little dirty. See you there!',
     NOW() - INTERVAL '5 days'),
    (4, 2, 'Cannot wait! I will bring some dog treats and a spare blanket for the cats.',
     NOW() - INTERVAL '4 days'),
    (4, 3, 'I can give people a lift from the city centre if anyone needs transport.',
     NOW() - INTERVAL '3 days'),

    -- Event 5 - Blood Donation Drive (upcoming)
    (5, 1, 'Great news -- the hospital has allocated 4 rooms for us. Breakfast will be provided for all donors!',
     NOW() - INTERVAL '2 days'),
    (5, 2, 'Wonderful! I donated six months ago so I am eligible again. Signing up now.',
     NOW() - INTERVAL '2 days'),
    (5, 4, 'I will definitely be there. Can I bring a few colleagues as well? Is there a limit per hour?',
     NOW() - INTERVAL '1 day'),
    (5, 1, 'No limit at all -- the more donors the better! Thank you, Elena.',
     NOW() - INTERVAL '1 day');

COMMIT;
