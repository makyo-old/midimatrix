from django.db import models
from django.contrib.auth.models import User

class Matrix(models.Model):
    author = models.ForeignKey(User)
    ctime = models.DateTimeField(auto_now_add = True)
    mtime = models.DateTimeField(auto_now = True)
    name = models.CharField(max_length = 250)
    description = models.TextField()
    matrix = models.TextField()
