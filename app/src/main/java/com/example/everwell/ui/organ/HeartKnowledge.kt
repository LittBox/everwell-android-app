package com.example.everwell.ui.organ

/** Offline, source-linked education content. Model names are matched exactly, never by screen area. */
internal data class HeartKnowledgeSource(
    val publisher: String,
    val title: String,
    val section: String,
    val url: String
)

internal data class HeartStructure(
    val id: String,
    val name: String,
    val englishName: String,
    val category: String,
    val summary: String,
    val flow: List<String>,
    val source: HeartKnowledgeSource,
    val modelNote: String? = null,
    val internalSpace: Boolean = false
) {
    // These IDs are the exact node names of the pinned BodyParts3D-derived GLB.
    val nodeNames: Set<String> get() = setOf(id)
}

internal object HeartKnowledge {
    const val MODEL_ASSET = "models/heart-structures.glb"
    const val MODEL_CREDITS_ASSET = "models/heart-structures-credits.txt"
    const val MODEL_SOURCE_URL = "https://github.com/organui/heart-3d/blob/c72ce6654ff8e282942f97750d3104f55ef4fc3d/docs/MODEL_PROVENANCE.md"
    const val MODEL_LICENSE_URL = "https://creativecommons.org/licenses/by/4.0/"
    const val MODEL_ATTRIBUTION = "BodyParts3D, © The Database Center for Life Science licensed under CC Attribution 4.0 International."

    private val bloodFlow = HeartKnowledgeSource(
        publisher = "NIH · NHLBI",
        title = "How Blood Flows through the Heart",
        section = "Adding oxygen to blood / Heart valves",
        url = "https://www.nhlbi.nih.gov/health/heart/blood-flow"
    )
    private val heartTissue = HeartKnowledgeSource(
        publisher = "NIH · NHLBI",
        title = "What the Heart Looks Like",
        section = "Heart chambers / Heart tissue",
        url = "https://www.nhlbi.nih.gov/health/heart/anatomy"
    )
    private val vessels = HeartKnowledgeSource(
        publisher = "OpenStax · Anatomy and Physiology 2e",
        title = "20.5 Circulatory Pathways",
        section = "Pulmonary circulation / The aorta / Systemic veins",
        url = "https://openstax.org/books/anatomy-and-physiology-2e/pages/20-5-circulatory-pathways"
    )
    private val coronary = bloodFlow.copy(section = "Supplying oxygen to the heart’s muscle")

    // Text is a short Chinese paraphrase of the linked references, checked 2026-11-12.
    // Mesh semantics come from the model manifest, not from interpreting geometry or colors.
    val structures: List<HeartStructure> = listOf(
        HeartStructure(
            id = "ascending-aorta", name = "升主动脉", englishName = "Ascending aorta",
            category = "大血管", summary = "主动脉离开心脏后向上延伸的起始段，承接左心室泵出的血液。",
            flow = listOf("左心室", "升主动脉", "主动脉弓"), source = vessels
        ),
        HeartStructure(
            id = "pulmonary-trunk", name = "肺动脉干", englishName = "Pulmonary trunk",
            category = "大血管", summary = "从右心室发出，分为左、右肺动脉，把贫氧血送往肺部进行气体交换。",
            flow = listOf("右心室", "肺动脉干", "左、右肺动脉"), source = vessels
        ),
        HeartStructure(
            id = "right-atrium", name = "右心房壁", englishName = "Right atrial wall",
            category = "心房", summary = "右心房接收从全身回流的血液，并将血液送入右心室。",
            flow = listOf("腔静脉", "右心房", "右心室"), source = bloodFlow,
            modelNote = "当前高亮的是右心房的壁面。"
        ),
        HeartStructure(
            id = "left-atrium", name = "左心房壁", englishName = "Left atrial wall",
            category = "心房", summary = "左心房接收经肺静脉返回的富氧血，再将血液送入左心室。",
            flow = listOf("肺静脉", "左心房", "左心室"), source = bloodFlow,
            modelNote = "当前高亮的是左心房的壁面，可转到背面观察。"
        ),
        HeartStructure(
            id = "ventricular-wall", name = "心室壁", englishName = "Ventricular wall",
            category = "心肌与腔室", summary = "心室壁包围心室腔，其中的心肌通过收缩和舒张参与泵血。",
            flow = emptyList(), source = heartTissue,
            modelNote = "模型将左右心室壁保留为一个整体。"
        ),
        HeartStructure(
            id = "right-ventricle", name = "右心室腔", englishName = "Right ventricular cavity",
            category = "心肌与腔室", summary = "右心室内容纳血液的空间。右心室把从右心房进入的血液泵向肺部。",
            flow = listOf("右心房", "右心室", "肺动脉干"), source = bloodFlow,
            modelNote = "展示的是腔室空间的形态，不是心肌；使用“单独查看”观察。",
            internalSpace = true
        ),
        HeartStructure(
            id = "left-ventricle", name = "左心室腔", englishName = "Left ventricular cavity",
            category = "心肌与腔室", summary = "左心室内容纳血液的空间。左心室将富氧血经主动脉瓣泵入主动脉，再送往全身。",
            flow = listOf("左心房", "左心室", "主动脉"), source = bloodFlow,
            modelNote = "展示的是腔室空间的形态，不是心肌；使用“单独查看”观察。",
            internalSpace = true
        ),
        HeartStructure(
            id = "aortic-arch", name = "主动脉弓", englishName = "Aortic arch",
            category = "大血管", summary = "主动脉转弯形成的弓形段，连接升主动脉与降主动脉。",
            flow = listOf("升主动脉", "主动脉弓", "降主动脉"), source = vessels
        ),
        HeartStructure(
            id = "superior-vena-cava", name = "上腔静脉", englishName = "Superior vena cava",
            category = "大血管", summary = "将上半身大部分区域回流的血液送入右心房，是静脉回心通路的一部分。",
            flow = listOf("上半身", "上腔静脉", "右心房"), source = vessels
        ),
        HeartStructure(
            id = "left-coronary", name = "左冠状动脉树", englishName = "Left coronary artery tree",
            category = "冠状循环", summary = "左冠状动脉及其分支为心脏组织输送富氧血，参与心肌自身的血液供应。",
            flow = listOf("主动脉", "左冠状动脉", "心脏组织"), source = coronary,
            modelNote = "当前高亮包含多条分支，作为一个整体展示。"
        ),
        HeartStructure(
            id = "right-coronary", name = "右冠状动脉树", englishName = "Right coronary artery tree",
            category = "冠状循环", summary = "右冠状动脉及其分支参与右心房和部分心室组织的血液供应。",
            flow = listOf("主动脉", "右冠状动脉", "心脏组织"), source = coronary,
            modelNote = "当前高亮包含多条分支，作为一个整体展示。"
        )
    )

    private val byId = structures.associateBy(HeartStructure::id)
    fun find(id: String?): HeartStructure? = id?.let(byId::get)
    fun findByNodeName(name: String?): HeartStructure? = find(name)
}
