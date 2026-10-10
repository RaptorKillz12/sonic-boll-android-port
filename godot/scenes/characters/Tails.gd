extends "res://scenes/characters/CharacterBase.gd"

func _ready() -> void:
	character_name = "Tails"
	move_speed = 360.0
	acceleration = 1500.0
	friction = 1800.0
	jump_velocity = -600.0
	gravity = 1400.0
	dash_speed = 660.0
	max_jumps = 2
	can_double_jump = true
	super._ready()

func jump() -> void:
	if is_on_floor():
		velocity.y = jump_velocity
		jump_count = 1
		emit_signal("character_jumped")
		return
	if jump_count < max_jumps:
		velocity.y = jump_velocity * 0.8
		jump_count += 1
		emit_signal("character_jumped")
		if has_node("AnimatedSprite2D"):
			$AnimatedSprite2D.play("fly")

func attack() -> void:
	if attack_timer > 0.0:
		return
	attack_timer = attack_cooldown
	emit_signal("character_attacked")
	if has_node("AnimatedSprite2D"):
		$AnimatedSprite2D.play("tails_attack")
