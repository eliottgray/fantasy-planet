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

#
# class GetElevationAtCoordinateTest(unittest.TestCase):
#
#     def test_low_resolution(self):
#         planet = Planet(resolution=5000)
#         elevation = planet.get_elevation_at(lat=0.0, lon=0.0)
#         self.assertIsInstance(Number, elevation)
