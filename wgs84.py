"""
Constants which define the World Geodetic System 1984 model.
See: https://en.wikipedia.org/wiki/World_Geodetic_System
"""

SEMI_MAJOR_AXIS = 6378137
FLATTENING = 1.0/298.257223563
SEMI_MINOR_AXIS = SEMI_MAJOR_AXIS * (1.0 - FLATTENING)
