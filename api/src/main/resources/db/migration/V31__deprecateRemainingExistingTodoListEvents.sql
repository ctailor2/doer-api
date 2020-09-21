UPDATE list_events
SET event_class = 'com.doerapispring.domain.events.DeprecatedEscalatedEvent'
WHERE event_class = 'com.doerapispring.domain.events.EscalatedEvent';

UPDATE list_events
SET event_class = 'com.doerapispring.domain.events.DeprecatedPulledEvent'
WHERE event_class = 'com.doerapispring.domain.events.PulledEvent';

UPDATE list_events
SET event_class = 'com.doerapispring.domain.events.DeprecatedUnlockedEvent'
WHERE event_class = 'com.doerapispring.domain.events.UnlockedEvent';
