package io.github.kgpu.kcgmath;

data class Point2(val x: Int = 0, val y: Int = 0){

    fun toPoint3(z : Int = 0) : Point3{
        return Point3(x, y, z)
    }

}

data class Point3(val x: Int = 0, val y: Int = 0, val z: Int = 0){

    fun toPoint2() : Point2 {
        return Point2(x, y)
    }

 }