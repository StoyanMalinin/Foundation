set search_path to foundation, public;

SELECT presences.timestamp as timestamp, st_x(presences.point) as x, st_y(presences.point) as y FROM
                        foundation.searches as searches JOIN foundation.search_to_presence as search_to_presence  ON searches.id = search_to_presence.search_id
                                 JOIN foundation.presences as presences ON search_to_presence.presence_id = presences.id
                        WHERE searches.id = 1 AND ST_Contains(ST_MakeEnvelope(-180, -85, 180, 85), point);
