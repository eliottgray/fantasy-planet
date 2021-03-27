import unittest
from tetrahedron import Tetrahedron, DEFAULT_A, DEFAULT_B, DEFAULT_C, DEFAULT_D
from point import Point


class TetrahedronTest(unittest.TestCase):

    def test_positive_case(self):
        a = Point(x=0.0, y=0.0, z=1.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=0.0, alt=1.0)
        c = Point(x=-1.0, y=1.0, z=0.0, alt=1.0)
        d = Point(x=0.0, y=-1.0, z=0.0, alt=1.0)
        tetra = Tetrahedron(a=a, b=b, c=c, d=d)
        self.assertEqual(a, tetra.a)
        self.assertEqual(b, tetra.b)
        self.assertEqual(c, tetra.c)
        self.assertEqual(d, tetra.d)

    def test_default(self):
        default = Tetrahedron.build_default()
        self.assertEqual(DEFAULT_A, default.a)
        self.assertEqual(DEFAULT_B, default.b)
        self.assertEqual(DEFAULT_C, default.c)
        self.assertEqual(DEFAULT_D, default.d)


class RotateTetrahedronTest(unittest.TestCase):

    def compare_points(self, expected_point, actual_point):
        self.assertEqual(expected_point, actual_point)

    def compare_tetrahedrons(self, expected_tetrahedron, actual_tetrahedron):
        self.compare_points(expected_tetrahedron.a, actual_tetrahedron.a)
        self.compare_points(expected_tetrahedron.b, actual_tetrahedron.b)
        self.compare_points(expected_tetrahedron.c, actual_tetrahedron.c)
        self.compare_points(expected_tetrahedron.d, actual_tetrahedron.d)

    def setUp(self) -> None:
        """Create a fixture that is quite obviously """
        a = Point(x=0.0, y=0.0, z=1.0, alt=1.0)   # Top of pyramid.
        b = Point(x=1.0, y=0.0, z=-1.0, alt=0.9)
        c = Point(x=-1.0, y=1.0, z=-1.0, alt=0.8)
        d = Point(x=-1.0, y=-1.0, z=-1.0, alt=0.7)
        self.tetra = Tetrahedron(a=a, b=b, c=c, d=d)

    def test_rotate_around_x_axis_90_degrees(self):
        a = Point(x=0.0, y=-1.0, z=0.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=0.0, alt=0.9)
        c = Point(x=-1.0, y=1.0, z=1.0, alt=0.8)
        d = Point(x=-1.0, y=1.0, z=-1.0, alt=0.7)
        expected = Tetrahedron(a=a, b=b, c=c, d=d)
        actual = self.tetra.rotate_around_x_axis(90)
        self.compare_tetrahedrons(expected_tetrahedron=expected, actual_tetrahedron=actual)


class ContainsPointTest(unittest.TestCase):

    def setUp(self) -> None:
        a = Point(x=0.0, y=0.0, z=1.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=0.0, alt=1.0)
        c = Point(x=-1.0, y=1.0, z=0.0, alt=1.0)
        d = Point(x=0.0, y=-1.0, z=0.0, alt=1.0)
        self.tetra = Tetrahedron(a=a, b=b, c=c, d=d)

    def test_vertex(self):
        """Point identical to a vertex of the tetrahedron."""
        vertex = self.tetra.a.copy()
        self.assertTrue(self.tetra.contains(vertex))

    def test_positive_case(self):
        """Point far inside the bounds of the tetrahedron."""
        point = Point(x=0.0, y=0.0, z=0.5, alt=0.0)
        self.assertTrue(self.tetra.contains(point))

    def test_negative_case(self):
        """Point far outside the bounds of the tetrahedron."""
        point = Point(x=0.0, y=0.0, z=-1.0, alt=0.0)
        self.assertFalse(self.tetra.contains(point))

    def test_on_edge(self):
        """By setting the fixture tetrahedron to include a right triangle, a point on the edge can be easily tested."""
        point = Point(x=0.0, y=-0.5, z=0.5, alt=0.0)
        self.assertTrue(self.tetra.contains(point))


class SideLengthTest(unittest.TestCase):

    def run_test(self, expected, tetra):
        actual = tetra._calculate_longest_side()
        self.assertAlmostEqual(expected, actual)

    def test_simple_case(self):
        """Side a-d longer than the others."""
        a = Point(x=1.0, y=0.0, z=1.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=-1.0, alt=1.0)
        c = Point(x=1.0, y=-1.0, z=-1.0, alt=1.0)
        d = Point(x=-1.0, y=0.0, z=-1.0, alt=1.0)
        tetra = Tetrahedron(a=a, b=b, c=c, d=d)
        expected = 2.8284271247461903
        self.run_test(expected, tetra)

    def test_rotated_case(self):
        """Side a-d is longer than the others, and the shape is rotated before calculating the length."""
        a = Point(x=1.0, y=0.0, z=1.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=-1.0, alt=1.0)
        c = Point(x=1.0, y=-1.0, z=-1.0, alt=1.0)
        d = Point(x=-1.0, y=0.0, z=-1.0, alt=1.0)
        tetra = Tetrahedron(a=a, b=b, c=c, d=d)
        tetra = tetra.rotate_around_x_axis(29)
        tetra = tetra.rotate_around_y_axis(123)
        expected = 2.8284271247461903
        self.run_test(expected, tetra)

    def test_equal_sides(self):
        a = Point(x=1.0, y=1.0, z=1.0, alt=1.0)
        b = Point(x=-1.0, y=-1.0, z=1.0, alt=1.0)
        c = Point(x=-1.0, y=1.0, z=-1.0, alt=1.0)
        d = Point(x=1.0, y=-1.0, z=-1.0, alt=1.0)
        tetra = Tetrahedron(a=a, b=b, c=c, d=d)
        expected = 2.8284271247461903
        self.run_test(expected, tetra)


class SubdivideTest(unittest.TestCase):

    def run_test(self, tetra: Tetrahedron, expected_one, expected_two):
        sub_a, sub_b = tetra.subdivide()
        # Because the returned tetrahedrons are in no particular order, we check both potential valid equality cases.
        try:
            self.assertEqual(expected_one, sub_a)
            self.assertEqual(expected_two, sub_b)
        except AssertionError:
            self.assertEqual(expected_one, sub_b)
            self.assertEqual(expected_two, sub_a)

    def test_simple_case(self):
        """Side a-d longer than the others."""
        a = Point(x=1.0, y=0.0, z=1.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=-1.0, alt=1.0)
        c = Point(x=1.0, y=-1.0, z=-1.0, alt=1.0)
        d = Point(x=-1.0, y=0.0, z=-1.0, alt=1.0)
        tetra = Tetrahedron(a=a, b=b, c=c, d=d)

        midpoint = a.midpoint(d)
        subdivided_a = Tetrahedron(a=a, b=b, c=c, d=midpoint)
        subdivided_d = Tetrahedron(a=midpoint, b=b, c=c, d=d)
        self.run_test(tetra, subdivided_a, subdivided_d)

    # TODO: Validate that, in the case of equal sides, edges are preferred in a guaranteed order.


class AverageAltitudeTest(unittest.TestCase):

    @staticmethod
    def construct_tetra(alt_1: float, alt_2: float, alt_3: float, alt_4: float) -> Tetrahedron:
        a = Point(x=0.0, y=0.0, z=1.0, alt=alt_1)
        b = Point(x=1.0, y=0.0, z=-1.0, alt=alt_2)
        c = Point(x=-1.0, y=1.0, z=-1.0, alt=alt_3)
        d = Point(x=-1.0, y=-1.0, z=-1.0, alt=alt_4)
        return Tetrahedron(a=a, b=b, c=c, d=d)

    def run_test(self, tetra: Tetrahedron, expected: float):
        actual = tetra.average_altitude()
        self.assertAlmostEqual(expected, actual)

    def test_identical_altitudes(self):
        altitude = 1.0
        tetra = self.construct_tetra(altitude, altitude, altitude, altitude)
        self.run_test(tetra, altitude)

    def test_different_altitudes(self):
        expected = 1.0
        tetra = self.construct_tetra(1.2, 1.1, 0.9, 0.8)
        self.run_test(tetra, expected)

    def test_tiny_difference(self):
        one = 1.000001
        two = 1.000003
        expected = 1.000002
        tetra = self.construct_tetra(one, one, two, two)
        self.run_test(tetra, expected)


class EqualityTest(unittest.TestCase):

    def run_test(self, one: Tetrahedron, two: Tetrahedron, expected_equality: bool):
        actual_equality = one == two
        self.assertEqual(expected_equality, actual_equality)
        actual_hash_equality = hash(one) == hash(two)
        self.assertEqual(expected_equality, actual_hash_equality)

    def test_identical_tetrahedrons(self):
        a = Point(x=1.0, y=0.0, z=1.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=-1.0, alt=1.0)
        c = Point(x=1.0, y=-1.0, z=-1.0, alt=1.0)
        d = Point(x=-1.0, y=0.0, z=-1.0, alt=1.0)
        one = Tetrahedron(a=a, b=b, c=c, d=d)
        two = Tetrahedron(a=a, b=b, c=c, d=d)
        self.run_test(one, two, expected_equality=True)

    def test_rearranged_points(self):
        a = Point(x=1.0, y=0.0, z=1.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=-1.0, alt=1.0)
        c = Point(x=1.0, y=-1.0, z=-1.0, alt=1.0)
        d = Point(x=-1.0, y=0.0, z=-1.0, alt=1.0)
        one = Tetrahedron(a=a, b=b, c=c, d=d)
        two = Tetrahedron(a=c, b=d, c=a, d=b)
        self.run_test(one, two, expected_equality=True)

    def test_different_point(self):
        a = Point(x=1.0, y=0.0, z=1.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=-1.0, alt=1.0)
        c = Point(x=1.0, y=-1.0, z=-1.0, alt=1.0)
        d = Point(x=-1.0, y=0.0, z=-1.0, alt=1.0)
        d_two = Point(x=0, y=0, z=0, alt=0)
        one = Tetrahedron(a=a, b=b, c=c, d=d)
        two = Tetrahedron(a=a, b=b, c=c, d=d_two)
        self.run_test(one, two, expected_equality=False)


class CopyTest(unittest.TestCase):

    def setUp(self) -> None:
        a = Point(x=1.0, y=0.0, z=1.0, alt=1.0)
        b = Point(x=1.0, y=1.0, z=-1.0, alt=1.0)
        c = Point(x=1.0, y=-1.0, z=-1.0, alt=1.0)
        d = Point(x=-1.0, y=0.0, z=-1.0, alt=1.0)
        self.tetra = Tetrahedron(a=a, b=b, c=c, d=d)
        self.copy = self.tetra.copy()

    def test_copy_is_equal(self):
        self.assertEqual(self.tetra, self.copy)

    def test_copy_has_different_memory_addresses(self):
        self.assertFalse(self.tetra is self.copy)
        # Copy must also copy points.
        self.assertFalse(self.tetra.a is self.copy.a)
        self.assertFalse(self.tetra.b is self.copy.b)
        self.assertFalse(self.tetra.c is self.copy.c)
        self.assertFalse(self.tetra.d is self.copy.d)

