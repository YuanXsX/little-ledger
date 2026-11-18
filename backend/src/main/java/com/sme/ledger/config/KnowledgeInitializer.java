package com.sme.ledger.config;

import com.sme.ledger.repository.KnowledgeDocRepository;
import com.sme.ledger.service.KnowledgeDocService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 财税知识库内置示例文档（幂等种子）：
 * 保证演示环境开箱即有可检索的知识内容，正式使用时可在知识库管理页删除。
 */
@Component
public class KnowledgeInitializer implements CommandLineRunner {

    private final KnowledgeDocRepository docRepository;
    private final KnowledgeDocService docService;

    public KnowledgeInitializer(KnowledgeDocRepository docRepository, KnowledgeDocService docService) {
        this.docRepository = docRepository;
        this.docService = docService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (docRepository.count() > 0) {
            return;
        }
        docService.saveFromText("小微企业增值税减免政策要点（示例）", VAT_POLICY, "system");
        docService.saveFromText("小微企业库存管理规范（示例）", INVENTORY_SPEC, "system");
        docService.saveFromText("小规模纳税人申报常见问题（示例）", TAX_FAQ, "system");
    }

    /** 示例文档内容（以税务机关最新文件为准） */
    private static final String VAT_POLICY = """
            小微企业增值税减免政策要点（示例文档，具体以税务机关最新文件为准）
            一、小规模纳税人免税政策
            对月销售额10万元以下（含本数）的增值税小规模纳税人，免征增值税。本政策执行至2027年12月31日。
            按月纳税的小规模纳税人月销售额未超过10万元，或按季纳税的季度销售额未超过30万元的，免征增值税。
            二、注意事项
            1. 免税销售额的口径包括销售货物、提供劳务、服务以及不动产转让等全部应税销售额，不含免税项目和不征税项目；
            2. 小规模纳税人开具增值税专用发票的部分，需照章缴纳增值税，不享受免税政策；
            3. 适用增值税差额征税政策的，以差额后的销售额确定是否可享受免税；
            4. 一般纳税人不得适用上述小规模纳税人免税政策。
            三、小微企业所得税优惠
            对小型微利企业年应纳税所得额不超过300万元的部分，减按25%计入应纳税所得额，按20%的税率缴纳企业所得税，实际税负为5%。
            小型微利企业标准：从事国家非限制和禁止行业，且同时符合年度应纳税所得额不超过300万元、从业人数不超过300人、资产总额不超过5000万元三个条件。
            四、「六税两费」减半征收
            增值税小规模纳税人、小型微利企业和个体工商户，可在50%的税额幅度内减征资源税、城市维护建设税、房产税、城镇土地使用税、印花税（不含证券交易印花税）、耕地占用税和教育费附加、地方教育附加。
            五、经营建议
            小微企业应关注月销售额是否接近10万元临界点，合理安排开票与申报节奏；免税额度用满后，超出的部分全额按征收率计算缴纳增值税，并非仅对超出部分征税。
            """;

    private static final String INVENTORY_SPEC = """
            小微企业库存管理规范（示例文档）
            一、最低库存阈值设置
            1. 常用商品（优先级 HIGH）最低库存建议不低于最近30天平均日销量的7倍；
            2. 非常用商品最低库存建议不低于最近90天平均日销量的3倍；
            3. 系统会对「库存数量 ≤ 最低库存阈值」的商品自动生成库存不足预警，请及时处理。
            二、出入库管理
            1. 每笔出入库必须在系统中登记日期与数量，做到账实同步；
            2. 建议每月至少进行一次全面盘点，盘点差异需查明原因后调整台账；
            3. 遵循先进先出原则（FIFO），有保质期的商品优先出库临期批次。
            三、台账要求
            1. 商品 SKU、名称、规格、单位应保持唯一和规范，避免一物多码；
            2. 供应商信息应完整登记，便于按供应商追溯库存分布与补货；
            3. 与客户台账关联的常采购商品，建议标记为高优先级，优先保障库存。
            四、风险防范
            1. 库存过低会造成断货损失，库存过高会占用资金，两者都应避免；
            2. 定期查看系统「异常预警」模块的库存不足提醒，未处理预警会重复提醒直至解决；
            3. 大额出入库操作保留单据备查，与财务流水台账的凭证号对应。
            """;

    private static final String TAX_FAQ = """
            小规模纳税人申报常见问题（示例文档）
            1. 问：小规模纳税人的申报周期是怎样的？
            答：增值税按季度申报，纳税期限为每个季度结束后15日内（遇法定节假日顺延）；企业所得税按季度预缴、年度汇算清缴。
            2. 问：季度销售额超过30万元怎么办？
            答：超过免税标准后，应就全部销售额按征收率（现行减按1%征收的政策以最新文件为准）计算缴纳增值税，而不是仅对超出部分缴税。
            3. 问：开具了增值税专用发票还能免税吗？
            答：专票对应的销售额不享受免税，需照章缴税；未开具专票的部分如果月销售额不超过10万元仍可免税。
            4. 问：没有收入是否需要申报？
            答：需要，即使销售额为0也应在申报期内进行零申报，逾期未申报可能产生罚款并影响纳税信用等级。
            5. 问：进项发票能否抵扣？
            答：小规模纳税人实行简易计税，取得的进项税额一般不能抵扣，应计入采购成本。
            6. 问：哪些收入不属于免税销售额口径？
            答：免税项目（如部分农产品自产自销）、不征税项目（如政府补贴）不占用10万元免税额度。
            7. 提醒：以上为通用示例说明，具体申报请以当地主管税务机关要求及最新政策文件为准。
            """;
}
