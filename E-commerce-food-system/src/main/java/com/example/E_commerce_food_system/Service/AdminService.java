package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.AdminDTO;
import java.util.List;

public interface AdminService {

    List<AdminDTO> getAllAdmins();      // ← must match exactly

    AdminDTO getAdminById(Integer id);

    AdminDTO getAdminByEmail(String email);

    AdminDTO createAdmin(AdminDTO adminDTO);

    AdminDTO updateAdmin(Integer id, AdminDTO adminDTO);

    void deleteAdmin(Integer id);

    AdminDTO login(String email, String password);
}