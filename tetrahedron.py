from point import Point
import numpy as np
import typing
import random

DEFAULT_ALTITUDE = -.02   # Just below 'sea level' of 0 altitude.


def tetra_coords(vertices):
    """
    Given a list of the xyz coordinates of the vertices of a tetrahedron,
    return tetrahedron coordinate system
    """
    origin, *rest = vertices
    mat = (np.array(rest) - origin).T
    tetra = np.linalg.inv(mat)
    return tetra, origin


def pointInside(point, tetra, origin):
    """
    Takes a single point or array of points, as well as tetra and origin objects returned by
    the Tetrahedron function.
    Returns a boolean or boolean array indicating whether the point is inside the tetrahedron.
    """
    newp = np.matmul(tetra, (point-origin).T).T
    return np.all(newp>=0, axis=-1) & np.all(newp <=1, axis=-1) & (np.sum(newp, axis=-1) <=1)


class Tetrahedron:

    def __init__(self, a: Point, b: Point, c: Point, d: Point):
        self.a = a
        self.b = b
        self.c = c
        self.d = d
        # TODO: Profile space/time difference of pre-generating a tuple for each point, or just defining them here.
        self._longest_side_len = None

    @staticmethod
    def build_default(seed, alt=25000000) -> 'Tetrahedron':
        """
        Creates a Tetrahedron with default orientation and altitudes.
        :return: Default Tetrahedron.
        """
        local_random = random.Random(seed)

        a = Point.from_spherical(lat=90, lon=0, alt=alt, seed=local_random.random())
        b = Point.from_spherical(lat=-30, lon=0, alt=alt, seed=local_random.random())
        c = Point.from_spherical(lat=-30, lon=120, alt=alt, seed=local_random.random())
        d = Point.from_spherical(lat=-30, lon=-120, alt=alt, seed=local_random.random())
        a.alt = DEFAULT_ALTITUDE
        b.alt = DEFAULT_ALTITUDE
        c.alt = DEFAULT_ALTITUDE
        d.alt = DEFAULT_ALTITUDE
        default = Tetrahedron(a=a, b=b, c=c, d=d)
        return default

    def rotate_around_x_axis(self, degrees: float) -> 'Tetrahedron':
        new_a = self.a.rotate_around_x_axis(degrees)
        new_b = self.b.rotate_around_x_axis(degrees)
        new_c = self.c.rotate_around_x_axis(degrees)
        new_d = self.d.rotate_around_x_axis(degrees)
        return Tetrahedron(a=new_a, b=new_b, c=new_c, d=new_d)

    def rotate_around_y_axis(self, degrees: float) -> 'Tetrahedron':
        new_a = self.a.rotate_around_y_axis(degrees)
        new_b = self.b.rotate_around_y_axis(degrees)
        new_c = self.c.rotate_around_y_axis(degrees)
        new_d = self.d.rotate_around_y_axis(degrees)
        return Tetrahedron(a=new_a, b=new_b, c=new_c, d=new_d)

    def contains(self, point: Point) -> bool:
        """
        Returns True if the given point lies inside the Tetrahedron, else False.
        Taken from Stack Overflow: https://stackoverflow.com/a/60745339
        """
        v1 = np.array(self.a.xyz)
        v2 = np.array(self.b.xyz)
        v3 = np.array(self.c.xyz)
        v4 = np.array(self.d.xyz)
        p = np.array(point.xyz)

        vertices = [v1, v2, v3, v4]
        tetra, origin = tetra_coords(vertices)
        inTet = pointInside(p, tetra, origin)
        return inTet

    def get_longest_side_length(self) -> float:
        if not self._longest_side_len:
            self._longest_side_len = self._calculate_longest_side()
        return self._longest_side_len

    def _calculate_longest_side(self) -> float:
        ab = self.a.distance(self.b)
        ac = self.a.distance(self.c)
        if ab >= ac:
            longest = ab
            e1 = self.a
            e2 = self.b
            n1 = self.c
            n2 = self.d
        else:
            longest = ac
            e1 = self.a
            e2 = self.c
            n1 = self.b
            n2 = self.d
        ad = self.a.distance(self.d)
        if ad > longest:
            longest = ad
            e1 = self.a
            e2 = self.d
            n1 = self.b
            n2 = self.c
        bc = self.b.distance(self.c)
        if bc > longest:
            longest = bc
            e1 = self.b
            e2 = self.c
            n1 = self.a
            n2 = self.d
        bd = self.b.distance(self.d)
        if bd > longest:
            longest = bd
            e1 = self.b
            e2 = self.c
            n1 = self.a
            n2 = self.d
        cd = self.c.distance(self.d)
        if cd > longest:
            longest = cd
            e1 = self.c
            e2 = self.d
            n1 = self.a
            n2 = self.b
        longest_side_len = longest
        self.a = e1
        self.b = e2
        self.c = n1
        self.d = n2
        return longest_side_len

    def subdivide(self) -> typing.Tuple['Tetrahedron', 'Tetrahedron']:
        length = self.get_longest_side_length()
        # Since calculating the longest side cached the longest edge as A->B, we can split A->B.
        midpoint = self.a.midpoint(self.b, length)
        tetra_one = Tetrahedron(a=self.a, b=midpoint, c=self.c, d=self.d)
        tetra_two = Tetrahedron(a=midpoint, b=self.b, c=self.c, d=self.d)
        return tetra_one, tetra_two

    def average_altitude(self):
        """
        :return:  Average altitude of the constituent points.
        """
        return (self.a.alt + self.b.alt + self.c.alt + self.d.alt) / 4

    def copy(self):
        """
        :return: New object with new points that have identical coordinates.
        """
        return Tetrahedron(
            a=self.a.copy(),
            b=self.b.copy(),
            c=self.c.copy(),
            d=self.d.copy()
        )

    def __repr__(self):
        return "Tetrahedron(a={}, b={}, c={}, d={})".format(repr(self.a), repr(self.b), repr(self.c), repr(self.d))

    def __eq__(self, other: 'Tetrahedron') -> bool:
        set_a = {self.a, self.b, self.c, self.d}
        set_b = {other.a, other.b, other.c, other.d}
        return set_a == set_b

    def __hash__(self):
        # TODO: Determine if there is a better way to permit any ordering of the points to result in the same hash.
        total = hash(self.a) + hash(self.b) + hash(self.c) + hash(self.d)
        return hash(total)
