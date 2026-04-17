CREATE TABLE IF NOT EXISTS harvests (
    id UUID NOT NULL,
    foreman_id UUID,
    harvest_date TIMESTAMP(6),
    harvester_id UUID NOT NULL,
    harvester_name VARCHAR(255) NOT NULL,
    news TEXT NOT NULL,
    photos VARCHAR ARRAY,
    plantation_id UUID NOT NULL,
    rejection_reason VARCHAR(255),
    status VARCHAR(255) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    status_updated_date TIMESTAMP(6),
    weight FLOAT NOT NULL,
    PRIMARY KEY (id)
);