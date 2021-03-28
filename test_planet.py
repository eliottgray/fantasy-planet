from planet import Planet, PlanetError
from numbers import Number
import unittest


class PlanetConstructorTest(unittest.TestCase):

    def test_positive_case(self):
        planet = Planet()
        self.assertIsInstance(planet.resolution, Number)

    def test_override_resolution(self):
        resolution = 3.6
        planet = Planet(resolution=resolution)
        self.assertEqual(resolution, planet.resolution)

    def test_invalid_depth(self):
        invalid_resolution = "I am not a numeric value."
        with self.assertRaises(PlanetError):
            Planet(resolution=invalid_resolution)


class GetElevationAtCoordinateTest(unittest.TestCase):

    def test_low_resolution(self):
        planet = Planet(resolution=100000.0)
        elevation = planet.get_elevation_at(lat=-10.0, lon=-43.0)
        self.assertIsInstance(elevation, Number)

    def test_high_resolution(self):
        planet = Planet(resolution=50.0)
        elevation = planet.get_elevation_at(lat=45.0, lon=23.0)
        self.assertIsInstance(elevation, Number)
