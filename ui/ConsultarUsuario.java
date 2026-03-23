package com.mycompany.gestorcontrasenyas.ui;

import com.mycompany.gestorcontrasenyas.model.Cuenta;
import com.mycompany.gestorcontrasenyas.service.CuentaService;
import com.mycompany.gestorcontrasenyas.service.RiotService;
import java.util.List;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class ConsultarUsuario extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ConsultarUsuario.class.getName());

    public ConsultarUsuario() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblUsuariosGuardados = new javax.swing.JTable();
        cmbCategoria = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        lblCargando = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                cargarTabla();
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 48));
        jLabel1.setText("Usuarios guardados");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18));
        jLabel2.setText("Categoria:");

        lblCargando.setFont(new java.awt.Font("Segoe UI", 2, 13));
        lblCargando.setForeground(new java.awt.Color(150, 150, 150));
        lblCargando.setText("");

        cmbCategoria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-", "Gmail", "Riot account", "Albion", "Otros" }));
        cmbCategoria.addActionListener(this::cmbCategoriaActionPerformed);

        tblUsuariosGuardados.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][] {},
            new String[] {}
        ));
        jScrollPane1.setViewportView(tblUsuariosGuardados);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(cmbCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblCargando)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cmbCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCargando))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(190, 190, 190)
                        .addComponent(jLabel1)))
                .addContainerGap(125, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>

    private void cmbCategoriaActionPerformed(java.awt.event.ActionEvent evt) {
        cargarTabla();
    }

    private void cargarTabla() {
        String categoriaSeleccionada = (String) cmbCategoria.getSelectedItem();

        // Preparar tabla vacía con las columnas correctas antes de lanzar el worker
        DefaultTableModel tabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (categoriaSeleccionada.equals("Riot account")) {
            tabla.setColumnIdentifiers(new String[]{"Categoria", "Usuario", "Contraseña", "Riot ID", "Rango Valorant", "Rango LoL"});
        } else {
            tabla.setColumnIdentifiers(new String[]{"Categoria", "Usuario", "Contraseña"});
        }
        tblUsuariosGuardados.setModel(tabla);

        // Deshabilitar el combo y mostrar mensaje mientras se cargan los datos
        cmbCategoria.setEnabled(false);
        lblCargando.setText("Cargando...");

        // SwingWorker: el trabajo pesado (llamadas a la API) se hace en un hilo aparte
        // para no congelar la interfaz
        SwingWorker<Void, Object[]> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() {
                // Este metodo se ejecuta en un hilo secundario, NO en el EDT
                List<Cuenta> listaCuentas = CuentaService.obtenerCuentas();

                for (Cuenta cuenta : listaCuentas) {
                    if (!categoriaSeleccionada.equals("-") && !cuenta.getCategoria().equals(categoriaSeleccionada)) {
                        continue;
                    }

                    if (categoriaSeleccionada.equals("Riot account")) {
                        String riotId       = cuenta.getRiotId();
                        String rangoValorant = "Sin rango";
                        String rangoLol      = "Sin rango";

                        if (riotId != null && !riotId.isEmpty()) {
                            String puuid = RiotService.obtenerPuuid(riotId);
                            if (puuid != null) {
                                rangoValorant = RiotService.obtenerRangoValorant(riotId);
                                rangoLol      = RiotService.obtenerRangoLol(puuid);
                            }
                        }
                        // publish envia la fila al EDT de forma segura mientras doInBackground sigue corriendo
                        publish(new Object[]{cuenta.getCategoria(), cuenta.getUsuario(), cuenta.getPassword(), riotId, rangoValorant, rangoLol});
                    } else {
                        publish(new Object[]{cuenta.getCategoria(), cuenta.getUsuario(), cuenta.getPassword()});
                    }
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Object[]> filas) {
                // Este metodo se ejecuta en el EDT — aqui es seguro tocar la UI
                // Se llama cada vez que doInBackground hace publish(), añadiendo filas progresivamente
                DefaultTableModel modelo = (DefaultTableModel) tblUsuariosGuardados.getModel();
                for (Object[] fila : filas) {
                    modelo.addRow(fila);
                }
            }

            @Override
            protected void done() {
                // Este metodo se ejecuta en el EDT cuando doInBackground ha terminado
                cmbCategoria.setEnabled(true);
                lblCargando.setText("");
            }
        };

        worker.execute();
    }

    // Variables declaration - do not modify
    private javax.swing.JComboBox<String> cmbCategoria;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lblCargando;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblUsuariosGuardados;
    // End of variables declaration
}