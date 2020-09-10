package com.doerapispring.domain

import java.util.Date

case class Snapshot[T](model: T, createdAt: Date)
