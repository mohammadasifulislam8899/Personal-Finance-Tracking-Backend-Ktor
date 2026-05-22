package com.xentoryx.finance_tracker.exception

class ValidationException(message: String) : Exception(message)
class AuthenticationException(message: String) : Exception(message)
class ForbiddenException(message: String) : Exception(message)
class NotFoundException(message: String) : Exception(message)
class ConflictException(message: String) : Exception(message)
