extends CharacterBody2D

# Sonic player character
# Handles movement, jumping, and collision

var speed = 300.0
var jump_force = -400.0
var gravity = 800.0

func _physics_process(delta):
	# Apply gravity
	velocity.y += gravity * delta
	
	# Handle input
	var input_velocity = Vector2.ZERO
	if Input.is_action_pressed("ui_right"):
		input_velocity.x = 1.0
	if Input.is_action_pressed("ui_left"):
		input_velocity.x = -1.0
	if Input.is_action_pressed("ui_up") and is_on_floor():
		velocity.y = jump_force
	
	# Apply movement
	if input_velocity.x != 0:
		velocity.x = input_velocity.x * speed
		$AnimatedSprite2D.play("run")
		$AnimatedSprite2D.flip_h = input_velocity.x < 0
	else:
		velocity.x = 0
		$AnimatedSprite2D.play("idle")
	
	# Move character
	move_and_slide()
