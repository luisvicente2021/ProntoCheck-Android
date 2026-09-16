import tensorflow as tf
import coremltools as ct
import numpy as np

tflite_path = "app/src/main/assets/mobile_facenet.tflite"

# Verificar que el modelo corre bien
interpreter = tf.lite.Interpreter(model_path=tflite_path)
interpreter.allocate_tensors()

input_idx  = interpreter.get_input_details()[0]['index']
output_idx = interpreter.get_output_details()[0]['index']

input_data = np.random.rand(1, 160, 160, 3).astype(np.float32)
interpreter.set_tensor(input_idx, input_data)
interpreter.invoke()
output = interpreter.get_tensor(output_idx)
print("✅ TFLite funciona, output shape:", output.shape)

# Construir modelo Keras equivalente y convertir
import tensorflow as tf

inputs = tf.keras.Input(shape=(160, 160, 3), batch_size=1)

@tf.function(input_signature=[tf.TensorSpec([1, 160, 160, 3], tf.float32)])
def infer(x):
    interpreter.set_tensor(input_idx, x.numpy())
    interpreter.invoke()
    return tf.constant(interpreter.get_tensor(output_idx))

# Guardar como concrete function
cf = infer.get_concrete_function()
mlmodel = ct.convert(cf, inputs=[ct.TensorType(name="x", shape=(1,160,160,3))])
mlmodel.save("mobile_facenet.mlpackage")
print("✅ Listo: mobile_facenet.mlpackage")
