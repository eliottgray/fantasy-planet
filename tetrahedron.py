from point import Point
from scipy.spatial import Delaunay
import numpy as np


class Tetrahedron:

    def __init__(self, a: Point, b: Point, c: Point, d: Point):
        self.a = a
        self.b = b
        self.c = c
        self.d = d
        # TODO: Profile space/time difference of pre-generating a tuple for each point, or just defining them here.
        # Stack overflow suggestion to use scipy Delaunay class: https://stackoverflow.com/a/16898636
        self.hull = Delaunay(np.array([(a.x, a.y, a.z), (b.x, b.y, b.z), (c.x, c.y, c.z), (d.x, d.y, d.z)]))

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
        point_array = np.array([(point.x, point.y, point.z)])
        simplex_array = self.hull.find_simplex(point_array)
        # The returned array of simplex points is only of length one, as we only query a single point at a time.
        # A value of -1 indicates that no triangle comprising the hull contains the point.
        return simplex_array[0] >= 0

    def longest_side_len(self) -> float:
        ab = self.a.distance(self.b)
        ac = self.a.distance(self.c)
        ad = self.a.distance(self.d)
        bc = self.b.distance(self.c)
        bd = self.b.distance(self.d)
        cd = self.c.distance(self.d)
        return max(ab, ac, ad, bc, bd, cd)
