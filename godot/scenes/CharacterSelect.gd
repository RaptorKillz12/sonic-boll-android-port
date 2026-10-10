extends Node

var character_scenes = {
	"sonic": preload("res://scenes/characters/Sonic.gd"),
	"tails": preload("res://scenes/characters/Tails.gd"),
	"knuckles": preload("res://scenes/characters/Knuckles.gd")
}

func spawn_character(character_key: String, position: Vector2 = Vector2.ZERO) -> CharacterBody2D:
	var scene_script = character_scenes.get(character_key.to_lower(), character_scenes["sonic"])
	var character = CharacterBody2D.new()
	character.set_script(scene_script)
	character.position = position
	add_child(character)
	return character

func choose_next_character(current: String) -> String:
	var keys = ["sonic", "tails", "knuckles"]
	var index = keys.find(current.to_lower())
	if index == -1:
		index = 0
	index = (index + 1) % keys.size()
	return keys[index]
