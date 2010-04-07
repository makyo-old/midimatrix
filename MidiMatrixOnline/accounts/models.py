from django.db import models
from django.contrib.auth.models import User

class Profile(models.Model):
    user = models.OneToOneField(User)
    badger = models.CharField(max_length = 30, blank = True)
    about = models.TextField(blank = True)
    interests = models.TextField(blank = True)
