extends CharacterBody2D

signal character_dashed
signal character_attacked
signal character_jumped

@export var character_name: String = "Base"
@export var move_speed: float = 300.0
@export var acceleration: float = 1600.0
@export var friction: float = 1800.0
@export var jump_velocity: float = -620.0
@export var gravity: float = 1500.0
@export var max_fall_speed: float = 980.0
@export var dash_speed: float = 720.0
@export var dash_duration: float = 0.18
@export var attack_cooldown: float = 0.35
@export var can_double_jump: bool = false

var direction: float = 1.0
var is_dashing: bool = false
var dash_timer: float = 0.0
var attack_timer: float = 0.0
var jump_count: int = 0
var max_jumps: int = 1
var facing_right: bool = true
var anim_state: String = "idle"

func _ready() -> void:
	if has_node("AnimatedSprite2D"):
		$AnimatedSprite2D.play("idle")
	set_physics_process(true)

func _physics_process(delta: float) -> void:
	apply_gravity(delta)
	handle_input(delta)
	apply_animation()
	move_and_slide()

func apply_gravity(delta: float) -> void:
	if not is_on_floor():
		velocity.y = min(velocity.y + gravity * delta, max_fall_speed)
	else:
		velocity.y = 0.0 if velocity.y >= 0.0 else velocity.y
		jump_count = 0

func handle_input(delta: float) -> void:
	var input_x = Input.get_action_strength("ui_right") - Input.get_action_strength("ui_left")
	if abs(input_x) > 0.05:
		direction = sign(input_x)
		facing_right = direction >= 0.0
		if is_dashing:
			velocity.x = direction * dash_speed
		else:
			velocity.x = move_toward(velocity.x, direction * move_speed, acceleration * delta)
	else:
		if is_dashing:
			velocity.x = direction * dash_speed
		else:
			velocity.x = move_toward(velocity.x, 0.0, friction * delta)
		if abs(velocity.x) < 2.0:
			velocity.x = 0.0

	if is_dashing:
		dash_timer -= delta
		if dash_timer <= 0.0:
			is_dashing = false
			velocity.x = direction * move_speed * 0.5

	if Input.is_action_just_pressed("ui_accept") or Input.is_action_just_pressed("jump"):
		jump()

	if Input.is_action_just_pressed("ui_focus_next") or Input.is_action_just_pressed("dash"):
		dash()

	if Input.is_action_just_pressed("ui_end") or Input.is_action_just_pressed("attack"):
		attack()

	if facing_right:
		scale.x = abs(scale.x)
	else:
		scale.x = -abs(scale.x)

func jump() -> void:
	if is_on_floor() or jump_count < max_jumps:
		if not is_on_floor() and jump_count > 0 and not can_double_jump:
			return
		velocity.y = jump_velocity
		jump_count += 1
		emit_signal("character_jumped")
		if has_node("AnimatedSprite2D"):
			$AnimatedSprite2D.play("jump")

func dash() -> void:
	if is_dashing:
		return
	is_dashing = true
	dash_timer = dash_duration
	velocity.x = direction * dash_speed
	velocity.y *= 0.7
	emit_signal("character_dashed")

func attack() -> void:
	if attack_timer > 0.0:
		return
	attack_timer = attack_cooldown
	emit_signal("character_attacked")
	if has_node("AnimatedSprite2D"):
		$AnimatedSprite2D.play("attack")

func apply_animation() -> void:
	if not has_node("AnimatedSprite2D"):
		return
	var sprite: AnimatedSprite2D = $AnimatedSprite2D
	if not is_on_floor():
		if velocity.y < 0:
			anim_state = "jump"
		else:
			anim_state = "fall"
	elif abs(velocity.x) > 10:
		anim_state = "run"
	else:
		anim_state = "idle"
	if sprite.animation != anim_state:
		sprite.play(anim_state)

func set_character_style(name: String) -> void:
	character_name = name
	if has_node("AnimatedSprite2D"):
		$AnimatedSprite2D.play("idle")

func get_character_name() -> String:
	return character_name

func get_state() -> Dictionary:
	return {
		"name": character_name,
		"speed": move_speed,
		"jump": jump_velocity,
		"direction": direction,
		"is_dashing": is_dashing,
		"on_floor": is_on_floor(),
	}

func _process(delta: float) -> void:
	if attack_timer > 0.0:
		attack_timer -= delta
		if attack_timer < 0.0:
			attack_timer = 0.0
