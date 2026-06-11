package com.example.E_commerce_food_system.Service;

import com.example.E_commerce_food_system.DTO.AdminDTO;
import com.example.E_commerce_food_system.Entity.Admin;
import com.example.E_commerce_food_system.Repository.AdminRepository;
import com.example.E_commerce_food_system.Service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;

    public AdminServiceImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    // Entity -> DTO
    private AdminDTO toDTO(Admin admin) {
        AdminDTO dto = new AdminDTO();
        dto.setAdminId(admin.getAdminId());
        dto.setFullName(admin.getFullName());
        dto.setEmail(admin.getEmail());
        dto.setPassword(admin.getPassword());
        dto.setCreatedAt(admin.getCreatedAt());
        return dto;
    }

    // DTO -> Entity
    private Admin toEntity(AdminDTO dto) {
        Admin admin = new Admin();
        admin.setFullName(dto.getFullName());
        admin.setEmail(dto.getEmail());
        admin.setPassword(dto.getPassword());
        admin.setCreatedAt(LocalDateTime.now());
        return admin;
    }

    @Override
    public List<AdminDTO> getAllAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AdminDTO getAdminById(Integer id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Admin not found with id: " + id));
        return toDTO(admin);
    }

    @Override
    public AdminDTO getAdminByEmail(String email) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Admin not found with email: " + email));
        return toDTO(admin);
    }

    @Override
    public AdminDTO createAdmin(AdminDTO adminDTO) {
        if (adminRepository.existsByEmail(adminDTO.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already exists: " + adminDTO.getEmail());
        }
        return toDTO(adminRepository.save(toEntity(adminDTO)));
    }

    @Override
    public AdminDTO updateAdmin(Integer id, AdminDTO adminDTO) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Admin not found with id: " + id));

        if (!admin.getEmail().equals(adminDTO.getEmail()) &&
                adminRepository.existsByEmail(adminDTO.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already exists: " + adminDTO.getEmail());
        }

        admin.setFullName(adminDTO.getFullName());
        admin.setEmail(adminDTO.getEmail());
        admin.setPassword(adminDTO.getPassword());

        return toDTO(adminRepository.save(admin));
    }

    @Override
    public void deleteAdmin(Integer id) {
        if (!adminRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Admin not found with id: " + id);
        }
        adminRepository.deleteById(id);
    }

    @Override
    public AdminDTO login(String email, String password) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!admin.getPassword().equals(password)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return toDTO(admin);
    }
}