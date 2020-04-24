package com.doerapispring.domain.events

import java.util.Date

case class TimestampedDomainEvent(domainEvent: DomainEvent, date: Date) extends DomainEvent
