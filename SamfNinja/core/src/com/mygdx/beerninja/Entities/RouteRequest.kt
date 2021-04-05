package com.mygdx.beerninja.Entities

// a route request is connected to a given button from the menu, and contains variables to describe the desired outcome of clicking it
class RouteRequest(var name: String, var description: String, var multiplayer: Boolean, var devMode: Boolean, var settings: Boolean)