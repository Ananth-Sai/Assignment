package com.taskflow.exception

class ResourceNotFoundException(message: String) : RuntimeException(message)

class UnauthorizedActionException(message: String) : RuntimeException(message)

class BadRequestException(message: String) : RuntimeException(message)

class ConflictException(message: String) : RuntimeException(message)

