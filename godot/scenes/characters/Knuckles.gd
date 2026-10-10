extends "res://scenes/characters/CharacterBase.gd"

func _ready() -> void:
	character_name = "Knuckles"
	move_speed = 290.0
	acceleration = 1300.0
	friction = 1700.0
	jump_velocity = -710.0
	gravity = 1700.0
	dash_speed = 620.0
	max_jumps = 1
	can_double_jump = false
	super._ready()

func attack() -> void:
	if attack_timer > 0.0:
		return
	attack_timer = attack_cooldown
	emit_signal("character_attacked")
	if has_node("AnimatedSprite2D"):
		$AnimatedSprite2D.play("punch")

func jump() -> void:
	if is_on_floor() or jump_count < max_jumps:
		velocity.y = jump_velocity
		jump_count += 1
		emit_signal("character_jumped")
		if has_node("AnimatedSprite2D"):
			$AnimatedSprite2D.play("jump")
