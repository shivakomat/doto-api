# --- !Ups

CREATE TABLE shopping_lists (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id  UUID         NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    created_by UUID         NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_shopping_lists_family_id ON shopping_lists(family_id);

CREATE TABLE shopping_items (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    list_id    UUID        NOT NULL REFERENCES shopping_lists(id) ON DELETE CASCADE,
    family_id  UUID        NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    name       VARCHAR(200) NOT NULL,
    category   VARCHAR(50)  NOT NULL DEFAULT 'other'
                            CHECK (category IN (
                                'produce', 'dairy', 'meat', 'bakery',
                                'frozen', 'household', 'personal_care',
                                'beverages', 'snacks', 'other'
                            )),
    quantity   VARCHAR(50),
    is_checked BOOLEAN      NOT NULL DEFAULT FALSE,
    checked_by UUID         REFERENCES profiles(id) ON DELETE SET NULL,
    checked_at TIMESTAMPTZ,
    created_by UUID         NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_shopping_items_list_id   ON shopping_items(list_id);
CREATE INDEX idx_shopping_items_family_id ON shopping_items(family_id);

# --- !Downs

DROP TABLE IF EXISTS shopping_items;
DROP TABLE IF EXISTS shopping_lists;
