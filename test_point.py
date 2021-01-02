import unittest
from point import Point, CoordinateError


class PointFromSphericalCoordinatesTest(unittest.TestCase):

    def run_test(self, lat=0.0, lon=0.0, alt=0.0, x=0.0, y=0.0, z=0.0):
        actual_point = Point.from_spherical(lat=lat, lon=lon, alt=alt)

        # Cartesian coordinates may be approximate, as they are the result of a transformation.
        self.assertAlmostEqual(x, actual_point.get_x())
        self.assertAlmostEqual(y, actual_point.get_y())
        self.assertAlmostEqual(z, actual_point.get_z())

        # Spherical coordinates must have been stored without being transformed.
        self.assertEqual(lat, actual_point.get_lat())
        self.assertEqual(lon, actual_point.get_lon())
        self.assertEqual(alt, actual_point.get_alt())

    def test_x_axis(self):
        """The Origin (0.0, 0.0) and it's Antipode represent the bounds of the X axis."""
        self.run_test(lat=0, lon=0, x=1)
        self.run_test(lat=0, lon=180, x=-1)
        self.run_test(lat=0, lon=-180, x=-1)

    def test_z_axis(self):
        """The poles represent the bounds of the Z axis."""
        self.run_test(lat=90.0, lon=0.0, z=1)
        self.run_test(lat=-90.0, lon=0.0, z=-1)

    def test_y_axis(self):
        """90 and negative 90 represent the bounds of the Y axis."""
        self.run_test(lat=0.0, lon=90, y=1)
        self.run_test(lat=0.0, lon=-90, y=-1)

    def test_all_axes(self):
        self.run_test(lat=45, lon=45, x=0.5, y=0.5, z=0.70710678)

    def test_invalid_latitude(self):
        with self.assertRaises(CoordinateError):
            self.run_test(lat=91)
        with self.assertRaises(CoordinateError):
            self.run_test(lat=-90.0001)

    def test_invalid_longitude(self):
        with self.assertRaises(CoordinateError):
            self.run_test(lon=181)
        with self.assertRaises(CoordinateError):
            self.run_test(lon=-180.0001)
