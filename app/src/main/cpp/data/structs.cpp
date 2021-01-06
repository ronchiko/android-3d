
#include <cmath>
#include <istream>

#include "data/structs.h"

Vec3 Vec3::operator+(const Vec3 &other) const noexcept {
    return {x + other.x, y + other.y, z + other.z};
}
Vec3 Vec3::operator-(const Vec3 &other) const noexcept {
    return {x - other.x, y - other.y, z - other.z };
}
Vec3 & Vec3::operator+=(const Vec3 &other) noexcept {
    x += other.x;
    y += other.y;
    z += other.z;
    return *this;
}
Vec3 & Vec3::operator-=(const Vec3 &other) noexcept {
    x -= other.x;
    y -= other.y;
    z -= other.z;
    return *this;
}

float Vec3::magnitude() const {
    return sqrt(x * x + y * y + z * z);
}

void Vec3::normalize() {
    float mag = magnitude();
    if(mag == 0) mag = 1;
    x /= mag; y /= mag; z /= mag;
}

Vec3 cross(const Vec3& a, const Vec3& b){
    Vec3 o;
    o.x = a.y * b.z - a.z * b.y;
    o.y = a.z * b.x - a.x * b.z;
    o.z = a.x * b.y - a.y * b.x;
    return  o;
}

std::istream& operator>>(std::istream& stream, Vec3& v){
    stream >> v.x;
    stream >> v.y;
    stream >> v.z;
    return stream;
}
std::istream& operator>>(std::istream& stream, Vec2& v){
    stream >> v.x;
    stream >> v.y;
    return stream;
}