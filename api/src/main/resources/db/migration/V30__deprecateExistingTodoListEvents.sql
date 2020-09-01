UPDATE list_events
SET event_class = 'com.doerapispring.domain.events.DeprecatedTodoUpdatedEvent'
WHERE event_class = 'com.doerapispring.domain.events.TodoUpdatedEvent';

UPDATE list_events
SET event_class = 'com.doerapispring.domain.events.DeprecatedTodoCompletedEvent'
WHERE event_class = 'com.doerapispring.domain.events.TodoCompletedEvent';

UPDATE list_events
SET event_class = 'com.doerapispring.domain.events.DeprecatedTodoDisplacedEvent'
WHERE event_class = 'com.doerapispring.domain.events.TodoDisplacedEvent';

UPDATE list_events
SET event_class = 'com.doerapispring.domain.events.DeprecatedTodoDeletedEvent'
WHERE event_class = 'com.doerapispring.domain.events.TodoDeletedEvent';

UPDATE list_events
SET event_class = 'com.doerapispring.domain.events.DeprecatedTodoAddedEvent'
WHERE event_class = 'com.doerapispring.domain.events.TodoAddedEvent';

UPDATE list_events
SET event_class = 'com.doerapispring.domain.events.DeprecatedDeferredTodoAddedEvent'
WHERE event_class = 'com.doerapispring.domain.events.DeferredTodoAddedEvent';

UPDATE list_events
SET event_class = 'com.doerapispring.domain.events.DeprecatedTodoMovedEvent'
WHERE event_class = 'com.doerapispring.domain.events.TodoMovedEvent';