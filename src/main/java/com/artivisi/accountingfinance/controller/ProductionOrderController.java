package com.artivisi.accountingfinance.controller;

import com.artivisi.accountingfinance.entity.BillOfMaterial;
import com.artivisi.accountingfinance.entity.ProductionOrder;
import com.artivisi.accountingfinance.entity.ProductionOrderStatus;
import com.artivisi.accountingfinance.service.BillOfMaterialService;
import com.artivisi.accountingfinance.service.ProductionOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/inventory/production")
@RequiredArgsConstructor
@Slf4j
public class ProductionOrderController {

    private final ProductionOrderService orderService;
    private final BillOfMaterialService bomService;

    @GetMapping
    public String list(@RequestParam(required = false) ProductionOrderStatus status, Model model) {
        if (status != null) {
            model.addAttribute("orders", orderService.findByStatus(status));
        } else {
            model.addAttribute("orders", orderService.findAll());
        }
        model.addAttribute("status", status);
        model.addAttribute("statuses", ProductionOrderStatus.values());
        return "inventory/production/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("order", new ProductionOrder());
        model.addAttribute("boms", bomService.findAll());
        return "inventory/production/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        return orderService.findById(id)
                .map(order -> {
                    if (!order.isDraft()) {
                        redirectAttributes.addFlashAttribute("error", "Hanya order dengan status DRAFT yang dapat diubah");
                        return "redirect:/inventory/production/" + id;
                    }
                    model.addAttribute("order", order);
                    model.addAttribute("boms", bomService.findAll());
                    return "inventory/production/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Production order tidak ditemukan");
                    return "redirect:/inventory/production";
                });
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        return orderService.findByIdWithLines(id)
                .map(order -> {
                    model.addAttribute("order", order);
                    return "inventory/production/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Production order tidak ditemukan");
                    return "redirect:/inventory/production";
                });
    }

    @PostMapping("/save")
    public String save(
            @RequestParam(required = false) UUID id,
            @RequestParam UUID bomId,
            @RequestParam BigDecimal quantity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate orderDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate plannedCompletionDate,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ProductionOrder order = new ProductionOrder();
            BillOfMaterial bom = new BillOfMaterial();
            bom.setId(bomId);
            order.setBillOfMaterial(bom);
            order.setQuantity(quantity);
            order.setOrderDate(orderDate);
            order.setPlannedCompletionDate(plannedCompletionDate);
            order.setNotes(notes);

            if (id == null) {
                ProductionOrder created = orderService.create(order);
                redirectAttributes.addFlashAttribute("success", "Production order " + created.getOrderNumber() + " berhasil dibuat");
            } else {
                orderService.update(id, order);
                redirectAttributes.addFlashAttribute("success", "Production order berhasil diperbarui");
            }

            return "redirect:/inventory/production";
        } catch (Exception e) {
            log.error("Error saving production order", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return id == null ? "redirect:/inventory/production/create" : "redirect:/inventory/production/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/start")
    public String start(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            ProductionOrder order = orderService.start(id);
            redirectAttributes.addFlashAttribute("success", "Production order " + order.getOrderNumber() + " sedang diproses");
        } catch (Exception e) {
            log.error("Error starting production order", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventory/production/" + id;
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            ProductionOrder order = orderService.complete(id);
            redirectAttributes.addFlashAttribute("success",
                    String.format("Production order %s selesai. Total biaya: Rp %,.0f, Harga pokok per unit: Rp %,.2f",
                            order.getOrderNumber(), order.getTotalComponentCost(), order.getUnitCost()));
        } catch (Exception e) {
            log.error("Error completing production order", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventory/production/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            ProductionOrder order = orderService.cancel(id);
            redirectAttributes.addFlashAttribute("success", "Production order " + order.getOrderNumber() + " dibatalkan");
        } catch (Exception e) {
            log.error("Error cancelling production order", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventory/production/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            orderService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Production order berhasil dihapus");
        } catch (Exception e) {
            log.error("Error deleting production order", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inventory/production";
    }
}
