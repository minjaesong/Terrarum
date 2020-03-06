package net.torvald.terrarum.worlddrawer

import com.badlogic.gdx.utils.Disposable
import net.torvald.UnsafeHelper

/**
 * As the fast access to this LUT is critical for the performance because of the way light calculation work,
 * even the IO can be a bottleneck so I use UnsafePointer.
 *
 * Created by Torvald on 2019-12-05.
 */
internal object LightmapHDRMap : Disposable {
    private val THE_LUT = floatArrayOf( // polynomial of 6.0   please refer to work_files/HDRcurveBezierLinIntp.kts
            0.0000f,0.0004f,0.0020f,0.0060f,0.0100f,0.0139f,0.0179f,0.0219f,0.0259f,0.0299f,0.0338f,0.0378f,0.0418f,0.0458f,0.0497f,0.0537f,
            0.0577f,0.0617f,0.0656f,0.0696f,0.0736f,0.0776f,0.0816f,0.0855f,0.0895f,0.0935f,0.0975f,0.1014f,0.1054f,0.1094f,0.1134f,0.1173f,
            0.1213f,0.1253f,0.1293f,0.1332f,0.1372f,0.1412f,0.1451f,0.1491f,0.1531f,0.1571f,0.1610f,0.1650f,0.1690f,0.1730f,0.1769f,0.1809f,
            0.1849f,0.1888f,0.1928f,0.1968f,0.2007f,0.2047f,0.2087f,0.2127f,0.2166f,0.2206f,0.2246f,0.2285f,0.2325f,0.2365f,0.2404f,0.2444f,
            0.2484f,0.2523f,0.2563f,0.2602f,0.2642f,0.2682f,0.2721f,0.2761f,0.2800f,0.2840f,0.2880f,0.2919f,0.2959f,0.2998f,0.3038f,0.3078f,
            0.3117f,0.3157f,0.3196f,0.3236f,0.3275f,0.3315f,0.3354f,0.3394f,0.3433f,0.3472f,0.3512f,0.3551f,0.3591f,0.3630f,0.3669f,0.3709f,
            0.3748f,0.3788f,0.3827f,0.3866f,0.3905f,0.3945f,0.3984f,0.4023f,0.4062f,0.4101f,0.4141f,0.4180f,0.4219f,0.4258f,0.4297f,0.4336f,
            0.4375f,0.4414f,0.4453f,0.4491f,0.4530f,0.4569f,0.4608f,0.4647f,0.4685f,0.4724f,0.4762f,0.4801f,0.4839f,0.4878f,0.4916f,0.4954f,
            0.4993f,0.5031f,0.5069f,0.5107f,0.5145f,0.5183f,0.5220f,0.5258f,0.5296f,0.5333f,0.5371f,0.5408f,0.5445f,0.5482f,0.5520f,0.5556f,
            0.5593f,0.5630f,0.5667f,0.5703f,0.5739f,0.5776f,0.5812f,0.5848f,0.5883f,0.5919f,0.5955f,0.5990f,0.6025f,0.6060f,0.6095f,0.6130f,
            0.6164f,0.6199f,0.6233f,0.6267f,0.6300f,0.6334f,0.6367f,0.6401f,0.6433f,0.6466f,0.6499f,0.6531f,0.6563f,0.6595f,0.6627f,0.6658f,
            0.6689f,0.6720f,0.6751f,0.6781f,0.6811f,0.6841f,0.6871f,0.6901f,0.6930f,0.6959f,0.6987f,0.7016f,0.7044f,0.7072f,0.7100f,0.7127f,
            0.7154f,0.7181f,0.7208f,0.7234f,0.7260f,0.7286f,0.7311f,0.7337f,0.7362f,0.7386f,0.7411f,0.7435f,0.7459f,0.7483f,0.7506f,0.7530f,
            0.7553f,0.7575f,0.7598f,0.7620f,0.7642f,0.7664f,0.7685f,0.7706f,0.7727f,0.7748f,0.7769f,0.7789f,0.7809f,0.7829f,0.7849f,0.7868f,
            0.7887f,0.7906f,0.7925f,0.7944f,0.7962f,0.7980f,0.7998f,0.8016f,0.8033f,0.8051f,0.8068f,0.8085f,0.8101f,0.8118f,0.8134f,0.8150f,
            0.8166f,0.8182f,0.8198f,0.8213f,0.8229f,0.8244f,0.8259f,0.8274f,0.8288f,0.8303f,0.8317f,0.8331f,0.8345f,0.8359f,0.8373f,0.8386f,
            0.8400f,0.8413f,0.8426f,0.8439f,0.8452f,0.8465f,0.8477f,0.8490f,0.8502f,0.8514f,0.8526f,0.8538f,0.8550f,0.8562f,0.8573f,0.8585f,
            0.8596f,0.8608f,0.8619f,0.8630f,0.8641f,0.8651f,0.8662f,0.8673f,0.8683f,0.8693f,0.8704f,0.8714f,0.8724f,0.8734f,0.8744f,0.8754f,
            0.8763f,0.8773f,0.8782f,0.8792f,0.8801f,0.8811f,0.8820f,0.8829f,0.8838f,0.8847f,0.8856f,0.8864f,0.8873f,0.8882f,0.8890f,0.8899f,
            0.8907f,0.8915f,0.8923f,0.8932f,0.8940f,0.8948f,0.8956f,0.8963f,0.8971f,0.8979f,0.8987f,0.8994f,0.9002f,0.9009f,0.9017f,0.9024f,
            0.9031f,0.9039f,0.9046f,0.9053f,0.9060f,0.9067f,0.9074f,0.9081f,0.9087f,0.9094f,0.9101f,0.9108f,0.9114f,0.9121f,0.9127f,0.9134f,
            0.9140f,0.9146f,0.9153f,0.9159f,0.9165f,0.9171f,0.9177f,0.9184f,0.9190f,0.9195f,0.9201f,0.9207f,0.9213f,0.9219f,0.9225f,0.9230f,
            0.9236f,0.9242f,0.9247f,0.9253f,0.9258f,0.9264f,0.9269f,0.9274f,0.9280f,0.9285f,0.9290f,0.9296f,0.9301f,0.9306f,0.9311f,0.9316f,
            0.9321f,0.9326f,0.9331f,0.9336f,0.9341f,0.9346f,0.9351f,0.9355f,0.9360f,0.9365f,0.9370f,0.9374f,0.9379f,0.9383f,0.9388f,0.9393f,
            0.9397f,0.9402f,0.9406f,0.9410f,0.9415f,0.9419f,0.9423f,0.9428f,0.9432f,0.9436f,0.9440f,0.9445f,0.9449f,0.9453f,0.9457f,0.9461f,
            0.9465f,0.9469f,0.9473f,0.9477f,0.9481f,0.9485f,0.9489f,0.9493f,0.9497f,0.9501f,0.9504f,0.9508f,0.9512f,0.9516f,0.9519f,0.9523f,
            0.9527f,0.9530f,0.9534f,0.9537f,0.9541f,0.9545f,0.9548f,0.9552f,0.9555f,0.9559f,0.9562f,0.9565f,0.9569f,0.9572f,0.9576f,0.9579f,
            0.9582f,0.9586f,0.9589f,0.9592f,0.9595f,0.9599f,0.9602f,0.9605f,0.9608f,0.9611f,0.9614f,0.9617f,0.9621f,0.9624f,0.9627f,0.9630f,
            0.9633f,0.9636f,0.9639f,0.9642f,0.9645f,0.9648f,0.9650f,0.9653f,0.9656f,0.9659f,0.9662f,0.9665f,0.9668f,0.9670f,0.9673f,0.9676f,
            0.9679f,0.9681f,0.9684f,0.9687f,0.9690f,0.9692f,0.9695f,0.9697f,0.9700f,0.9703f,0.9705f,0.9708f,0.9711f,0.9713f,0.9716f,0.9718f,
            0.9721f,0.9723f,0.9726f,0.9728f,0.9731f,0.9733f,0.9735f,0.9738f,0.9740f,0.9743f,0.9745f,0.9747f,0.9750f,0.9752f,0.9754f,0.9757f,
            0.9759f,0.9761f,0.9764f,0.9766f,0.9768f,0.9770f,0.9773f,0.9775f,0.9777f,0.9779f,0.9781f,0.9784f,0.9786f,0.9788f,0.9790f,0.9792f,
            0.9794f,0.9796f,0.9799f,0.9801f,0.9803f,0.9805f,0.9807f,0.9809f,0.9811f,0.9813f,0.9815f,0.9817f,0.9819f,0.9821f,0.9823f,0.9825f,
            0.9827f,0.9829f,0.9831f,0.9832f,0.9834f,0.9836f,0.9838f,0.9840f,0.9842f,0.9844f,0.9846f,0.9847f,0.9849f,0.9851f,0.9853f,0.9855f,
            0.9856f,0.9858f,0.9860f,0.9862f,0.9864f,0.9865f,0.9867f,0.9869f,0.9870f,0.9872f,0.9874f,0.9876f,0.9877f,0.9879f,0.9881f,0.9882f,
            0.9884f,0.9886f,0.9887f,0.9889f,0.9890f,0.9892f,0.9894f,0.9895f,0.9897f,0.9898f,0.9900f,0.9901f,0.9903f,0.9905f,0.9906f,0.9908f,
            0.9909f,0.9911f,0.9912f,0.9914f,0.9915f,0.9917f,0.9918f,0.9920f,0.9921f,0.9922f,0.9924f,0.9925f,0.9927f,0.9928f,0.9930f,0.9931f,
            0.9932f,0.9934f,0.9935f,0.9937f,0.9938f,0.9939f,0.9941f,0.9942f,0.9943f,0.9945f,0.9946f,0.9947f,0.9949f,0.9950f,0.9951f,0.9953f,
            0.9954f,0.9955f,0.9957f,0.9958f,0.9959f,0.9960f,0.9962f,0.9963f,0.9964f,0.9965f,0.9967f,0.9968f,0.9969f,0.9970f,0.9971f,0.9973f,
            0.9974f,0.9975f,0.9976f,0.9977f,0.9978f,0.9980f,0.9981f,0.9982f,0.9983f,0.9984f,0.9985f,0.9987f,0.9988f,0.9989f,0.9990f,0.9991f,
            0.9992f,0.9993f,0.9994f,0.9995f,0.9996f,0.9997f,0.9999f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,
            1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f,1.0000f, 1f  // isn't it beautiful?
    )

    val ptr = UnsafeHelper.allocate(4L * THE_LUT.size)

    operator fun invoke() {
        THE_LUT.forEachIndexed { i, f ->
            ptr.setFloat(4L * i, f)
        }
    }

    inline operator fun get(index: Int) = ptr.getFloat(4L * index)

    val lastIndex = THE_LUT.lastIndex

    override fun dispose() {
        ptr.destroy()
    }
}