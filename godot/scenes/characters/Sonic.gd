extends "res://scenes/characters/CharacterBase.gd"

func _ready() -> void:
	character_name = "Sonic"
	move_speed = 420.0
	acceleration = 1800.0
	friction = 2000.0
	jump_velocity = -650.0
	gravity = 1500.0
	dash_speed = 760.0
	max_jumps = 1
	can_double_jump = true
	super._ready()

func dash() -> void:
	if is_dashing:
		return
	is_dashing = true
	dash_timer = dash_duration
	velocity.x = direction * dash_speed
	velocity.y *= 0.5
	emit_signal("character_dashed")
	if has_node("AnimatedSprite2D"):
		$AnimatedSprite2D.play("dash")

func attack() -> void:
	if attack_timer > 0.0:
		return
	attack_timer = attack_cooldown
	emit_signal("character_attacked")
	if has_node("AnimatedSprite2D"):
		$AnimatedSprite2D.play("spin")
